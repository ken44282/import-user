(ns import-user.core
  (:gen-class)
  (:require [clojure.java.io :as io]
            [clojure.data.csv :as csv]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [hugsql.core :as hugsql])
  (:import (java.util Properties)))

(def conf (Properties.))
(.load conf (ClassLoader/getSystemResourceAsStream
              "system.properties"))

(def db {:subprotocol (.getProperty conf "db.subprotocol")
         :subname (.getProperty conf "db.subname")
         :user (.getProperty conf "db.user")
         :password (.getProperty conf "db.password")})

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
        (when (.isFile f) (.delete f))
        (.renameTo
          f
          (io/file (.getProperty conf "dir.done")
                   (.getName f)))))))

(defn output-users []
  (let [output-file (io/file (.getProperty conf "dir.result")
                             "users.csv")]
    (with-open [writer (io/writer output-file)]
      (->> (select-users db {})
           (map #(vector (:id %) (:name %) (:pass %)))
           (csv/write-csv writer)))))

(defn -main [& args]
  (do
    (log/info "**** import-user start ****")
    (->> (.getProperty conf "dir.spool")
         (io/file)
         (file-seq)
         (filter #(.isFile %))
         (map #(file-proc %))
         (doall))
    (output-users)
    (log/info "**** import-user end ****")))