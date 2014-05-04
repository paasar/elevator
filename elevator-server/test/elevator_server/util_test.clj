(ns elevator-server.util-test
  (:require [clojure.test :refer :all]
            [elevator-server.util :refer :all]))

(deftest keep-floor-target-within-boundaries
  (testing "min"
    (let [result (keep-floor-target-inside-boundaries 0 1 5)]
      (is (= 1 result))))

  (testing "middle"
    (let [result (keep-floor-target-inside-boundaries 2 1 5)]
      (is (= 2 result))))

  (testing "max"
    (let [result (keep-floor-target-inside-boundaries 6 1 5)]
      (is (= 5 result)))))