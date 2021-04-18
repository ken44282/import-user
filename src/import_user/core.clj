(ns import-user.core
  (:gen-class)
  (:require [clojure.java.io :as io]
            [clojure.data.csv :as csv]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [cprop.core :refer [load-config]]
            [cprop.source :as source]
            [hugsql.core :as hugsql]))

(def conf (load-config
            :merge
            [(source/from-system-props)
             (source/from-env)]))

(def db {:subprotocol (:import-user-db-subprotocol conf)
         :subname (:import-user-db-subname conf)
         :user (:import-user-db-user conf)
         :password (:import-user-db-password conf)})

(hugsql/def-db-fns "users.sql")

(defn line-proc [tx line]
  (do
    (log/debug line)
    (case (first line)
      "I" (insert-users tx {:id   (nth line 1),
                            :name (nth line 2)
                            :pass (nth line 3)})
      "U" (update-users tx {:id   (nth line 1)
                            :name (nth line 2)
                            :pass (nth line 3)})
      "D" (delete-users tx {:id (nth line 1)}))))

(defn file-proc [f]
  (do
    (log/debug (.toString f))
    (try
      (jdbc/with-db-transaction
        [tx db]
        (with-open [reader (io/reader f)]
          (->> (csv/read-csv reader)
               (map #(line-proc tx %))
               (doall))))
      (catch Exception e
        (log/error e "Error:" (.toString f)))
      (finally
        (let [dest-file (io/file (:import-user-dir-done conf) (.getName f))]
          (when (.isFile dest-file) (.delete dest-file))
          (.renameTo
            f
            dest-file))))))

(defn output-users []
  (let [output-file (io/file (:import-user-dir-result conf) "users.csv")]
    (with-open [writer (io/writer output-file)]
      (->> (select-users db {})
           (map #(vector (:id %) (:name %) (:pass %)))
           (csv/write-csv writer)))))

(defn -main [& args]
  (do
    (log/info "**** import-user start ****")
    (->> (:import-user-dir-spool conf)
         (io/file)
         (file-seq)
         (filter #(.isFile %))
         (map #(file-proc %))
         (doall))
    (output-users)
    (log/info "**** import-user end ****")))