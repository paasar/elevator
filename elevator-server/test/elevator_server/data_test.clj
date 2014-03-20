(ns elevator-server.data-test
  (:require [clojure.test :refer :all]
            [elevator-server.data :refer :all]
            [cheshire.core :as json]))

(defn check-empty-floor [floor number]
  (is (= (:number floor) number))
  (is (= (:waiting floor) [])))

(deftest floor-creation
  (testing "floor creation"
    (let [fifth-floor (create-floor 5)
          sixth-floor (create-floor 6)]
      (check-empty-floor fifth-floor 5)
      (check-empty-floor sixth-floor 6)))

  (testing "floors creation"
    (let [nine-floors (create-floors 9)
          seven-floors (create-floors 7)]
      (is (= (count nine-floors) 9))
      (is (= (count seven-floors) 7)))))

(deftest floor-manipulation
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

(defn set-up-state-for-transformation [internal-state]
  (-> internal-state
    (assoc-in [:elevator :to-requests] [3 2])
    (add-next-request {:from 5 :to 3})
    (assoc :floors [{:number 1 :waiting [1 3]}])))

(def expected-public-data
  (json/parse-string (slurp "resources/test/public-state.json") true))

(deftest state-manipulation
  (testing "create new state"
    (let [new-state (create-new-state-data)]
      (is (= (:from-requests new-state) []))
      (is (= (count (:floors new-state)) number-of-floors))))

  (testing "transform floor to public"
    (let [public-1 (transform-floor-to-public {:number 1, :waiting [1 5]} 3)
          public-2 (transform-floor-to-public {:number 3, :waiting [2 1 5 6 7]} 4)
          public-3 (transform-floor-to-public {:number 2, :waiting []} 4)]
      (is (= {:number 1 :waiting 1 :impatient 1} public-1))
      (is (= {:number 3 :waiting 2 :impatient 3} public-2))
      (is (= {:number 2 :waiting 0 :impatient 0} public-3))))

  (testing "transform state into public form"
    (let [internal-state (set-up-state-for-transformation (create-new-state-data))
          public-data (transform-state-to-public internal-state)]
      (is (= public-data expected-public-data))))

  (testing "add new request"
    (let [request {:from 2 :to 3}
          state-with-request (add-next-request (create-new-state-data) request)]
      (is (= request (first (:from-requests state-with-request)))))))