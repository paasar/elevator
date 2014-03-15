(ns elevator-server.data-test
  (:require [clojure.test :refer :all]
            [elevator-server.data :refer :all]
            [cheshire.core :as json]))

(defn check-empty-floor [floor number]
  (is (= (:number floor) number))
  (is (= (:waiting floor) 0))
  (is (= (:impatient floor) 0)))

(def expected-public-data
  (json/parse-string (slurp "resources/test/public-state.json") true))

(deftest data-handling
  (testing "floor creation"
    (let [fifth-floor (create-floor 5)
          sixth-floor (create-floor 6)]
      (check-empty-floor fifth-floor 5)
      (check-empty-floor sixth-floor 6)))

  (testing "floors creation"
    (let [nine-floors (create-floors 9)
          seven-floors (create-floors 7)]
      (is (= (count nine-floors) 9))
      (is (= (count seven-floors) 7))))

  (testing "create new state"
    (let [new-state (create-new-state-data)]
      (is (= (:from-requests new-state) []))
      (is (= (count (:floors new-state)) number-of-floors))))

  (testing "generate request"
    (let [floors 5
          generated-request (generate-request floors)
          from (:from generated-request)
          to (:to generated-request)]
      (is (not (nil? from)))
      (is (not (nil? to)))
      (is (not (= from to)))
      (is (and (> from 0) (< from (inc floors))))
      (is (and (> to 0) (< to (inc floors)))))))

;  (testing "transform state into public form"
;    (let [public-data (transform-state-to-public (create-new-state-data))]
;      (is (= public-data expected-filtered-data))))
;  )