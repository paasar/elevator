(ns elevator-server.request-generator-test
  (:require [clojure.test :refer :all]
            [elevator-server.request-generator :refer :all]))

(deftest single-request-generation
  (testing "generate single request"
    (let [floors 5
          generated-request (generate-random-request floors)
          from (:from generated-request)
          to (:to generated-request)]
      (is (not (nil? from)))
      (is (not (nil? to)))
      (is (not (= from to)))
      (is (and (> from 0) (<= from floors)))
      (is (and (> to 0) (<= to floors)))))

  (testing "mitigates out of boundaries start floor"
    (is (= 5 (:from (generate-random-request-from 5 6))))
    (is (= 1 (:from (generate-random-request-from 5 0))))))