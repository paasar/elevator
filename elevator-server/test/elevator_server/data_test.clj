(ns elevator-server.data-test
  (:require [clojure.test :refer :all]
            [elevator-server.data :refer :all]
            [cheshire.core :as json]))

(deftest floor-manipulation
  (testing "generate request"
    (let [floors 5
          generated-request (generate-request floors)
          from (:from generated-request)
          to (:to generated-request)]
      (is (not (nil? from)))
      (is (not (nil? to)))
      (is (not (= from to)))
      (is (and (> from 0) (<= from floors)))
      (is (and (> to 0) (<= to floors))))))

(def patient-request {:from 5 :to 3 :waited 1})
(def impatient-request {:from 5 :to 3 :waited 6})

(defn set-up-state-for-transformation [internal-state-data]
  (-> internal-state-data
    (assoc-in [:elevator :to-requests] [3 2])
    (add-next-request patient-request)
    (assoc :floors 5)))

(def expected-public-data
  (json/parse-string (slurp "resources/test/public-state.json") true))


(deftest state-manipulation
  (testing "create new state"
    (let [new-state (create-new-state-data)]
      (is (= (:from-requests new-state) []))
      (is (= (:floors new-state) number-of-floors))))

  (testing "transform single state data into public form"
    (let [internal-state-data (set-up-state-for-transformation (create-new-state-data))
          public-data (transform-state-to-public internal-state-data)]
      (is (= public-data expected-public-data))))

  (testing "add new request"
    (let [request {:from 2 :to 3}
          state-with-request (add-next-request (create-new-state-data) request)]
      (is (= request (first (:from-requests state-with-request))))))

  (testing "transform full internal state to public"
    (let [internal-state (vector (set-up-state-for-transformation (create-new-state-data))
                                 (set-up-state-for-transformation (create-new-state-data)))
          public-state (transform-internal-state-to-public internal-state)
          expected-result (vector expected-public-data expected-public-data)]
      (is (= public-state expected-result))))

  (testing "transform patient request"
    (let [transformed-state (transform-from-request-to-public patient-request)]
      (is (= {:floor 5 :direction "down" :impatient false} transformed-state))))

  (testing "transform impatient request"
    (let [transformed-state (transform-from-request-to-public impatient-request)]
      (is (= {:floor 5 :direction "down" :impatient true} transformed-state)))))