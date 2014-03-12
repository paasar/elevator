(ns elevator-server.core-test
  (:require [clojure.test :refer :all]
            [elevator-server.core :refer :all])
  (:use ring.mock.request
        elevator-server.core))

(deftest test-app
;  (testing "main route"
;    (let [response (app (request :get "/"))]
;      (is (= (:status response) 200))
;      (is (= (:body response) "Hello World"))))

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= (:status response) 404)))))

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
      (is (= (count seven-floors) 7)))))