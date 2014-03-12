(ns elevator-server.data-test
  (:require [clojure.test :refer :all]
            [elevator-server.data :refer :all]))

(deftest data-handling
  (testing "floor creation"
    (let [fifth-floor (create-floor 5)
          sixth-floor (create-floor 6)]
      (is (= (:number fifth-floor) 5))
      (is (= (:waiting fifth-floor) 0))
      (is (= (:impatient fifth-floor) 0))
      (is (= (:number sixth-floor) 6))
      (is (= (:waiting sixth-floor) 0))
      (is (= (:impatient sixth-floor) 0))))

  (testing "floors creation"
    (let [nine-floors (create-floors 9)
          seven-floors (create-floors 7)]
      (is (= (count nine-floors) 9))
      (is (= (count seven-floors) 7))))

  (testing "create new state"
    (let [new-state (create-new-state-data)]
      (is (= (:from-requests new-state) []))
      (is (= (count (:floors new-state)) default-number-of-floors)))))