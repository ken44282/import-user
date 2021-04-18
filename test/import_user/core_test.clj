(ns import-user.core-test
  (:require [clojure.test :refer :all]
            [import-user.core :refer :all]
            [clojure.java.io :as io]
            [clojure.data.csv :as csv]
            [clojure.java.jdbc :as jdbc]))

(def test-data [["I" "user1" "USER001" "pass001"]
                ["I" "user2" "USER002" "pass002"]
                ["I" "user3" "USER003" "pass003"]])
(def input-file (io/file (:import-user-dir-spool conf) "test1.csv"))
(def output-file (io/file (:import-user-dir-result conf) "users.csv"))

(deftest a-test
  (testing "insert-test"
    (do
      (jdbc/delete! db :users {})
      (with-open [writer
                  (io/writer (io/file (:import-user-dir-spool conf) "test1.csv"))]
        (csv/write-csv writer [["I" "user1" "USER001" "pass001"]
                               ["I" "user2" "USER002" "pass002"]
                               ["I" "user3" "USER003" "pass003"]]))
      (-main)
      (is (= [["user1" "USER001" "pass001"]
              ["user2" "USER002" "pass002"]
              ["user3" "USER003" "pass003"]]
             (with-open [reader
                         (io/reader (io/file (:import-user-dir-result conf) "users.csv"))]
               (apply vector (csv/read-csv reader))))))))

(deftest b-test
  (testing "insert-update-delete-test"
    (do
      (jdbc/delete! db :users {})
      (with-open [writer
                  (io/writer (io/file (:import-user-dir-spool conf) "test1.csv"))]
        (csv/write-csv writer [["I" "user1" "USER001" "pass001"]
                               ["I" "user2" "USER002" "pass002"]
                               ["I" "user3" "USER003" "pass003"]]))
      (-main)
      (with-open [writer
                  (io/writer (io/file (:import-user-dir-spool conf) "test2.csv"))]
        (csv/write-csv writer [["I" "user4" "USER004" "pass004"]
                               ["U" "user1" "user001" "PASS001"]
                               ["D" "user3" "USER003" "pass003"]]))
      (-main)
      (is (= [["user1" "user001" "PASS001"]
              ["user2" "USER002" "pass002"]
              ["user4" "USER004" "pass004"]]
             (with-open [reader
                         (io/reader (io/file (:import-user-dir-result conf) "users.csv"))]
               (apply vector (csv/read-csv reader))))))))

(deftest c-test
  (testing "rollback-test"
    (do
      (jdbc/delete! db :users {})
      (with-open [writer
                  (io/writer (io/file (:import-user-dir-spool conf) "test1.csv"))]
        (csv/write-csv writer [["I" "user1" "USER001" "pass001"]
                               ["I" "user2" "USER002" "pass002"]
                               ["I" "user3" "USER003" "pass003"]]))
      (-main)
      (with-open [writer
                  (io/writer (io/file (:import-user-dir-spool conf) "test2.csv"))]
        (csv/write-csv writer [["I" "user4" "USER004" "pass004"]
                               ["I" "user1" "user001" "PASS001"]
                               ["D" "user3" "USER003" "pass003"]]))
      (-main)
      (with-open [writer
                  (io/writer (io/file (:import-user-dir-spool conf) "test3.csv"))]
        (csv/write-csv writer [["I" "user5" "USER005" "pass005"]]))
      (-main)
      (is (= [["user1" "USER001" "pass001"]
              ["user2" "USER002" "pass002"]
              ["user3" "USER003" "pass003"]
              ["user5" "USER005" "pass005"]]
             (with-open [reader
                         (io/reader (io/file (:import-user-dir-result conf) "users.csv"))]
               (apply vector (csv/read-csv reader))))))))