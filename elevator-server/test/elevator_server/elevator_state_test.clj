(ns elevator-server.elevator-state-test
  (:require [clojure.test :refer :all]
            [elevator-server.elevator-state :refer :all]
            [elevator-server.data :as data :refer [create-new-player-state]]))

(defn create-state-with-defined-elevator [state current-floor target-floor]
  (-> (create-new-player-state)
    (assoc-in [:elevator :state] state)
    (assoc-in [:elevator :current-floor] current-floor)
    (assoc-in [:elevator :going-to] target-floor)))

(deftest elevator-states
  (testing "same floor gives waiting"
    (is (= :waiting (get-ascending-or-descending-or-waiting 1 1))))
  (testing "higher target floor gives ascending"
    (is (= :ascending (get-ascending-or-descending-or-waiting 1 2))))
  (testing "lower target floor gives waiting"
    (is (= :descending (get-ascending-or-descending-or-waiting 2 1))))

  (testing "setting elevator to go up"
    (let [before-state (data/create-new-player-state)
          after-state (set-new-target-floor before-state 2)
          elevator (:elevator after-state)]
      (is (= 1 (:current-floor elevator)))
      (is (= 2 (:going-to elevator)))
      (is (= :ascending (:state elevator)))))

  (testing "move elevator up, not reaching target"
    (let [before-state (create-state-with-defined-elevator :ascending 1 3)
          after-state (update-elevator-state before-state)
          elevator (:elevator after-state)]
      (is (= 2 (:current-floor elevator)))
      (is (= :ascending (:state elevator)))))

  (testing "move elevator up, reaching target, no riders, no newcomers"
    (let [before-state (create-state-with-defined-elevator :ascending 2 3)
          after-state (update-elevator-state before-state)
          elevator (:elevator after-state)]
      (is (= 3 (:current-floor elevator)))
      (is (= :waiting (:state elevator)))))

  (testing "move elevator up, reaching target, riders"
    (let [before-state (-> (create-state-with-defined-elevator :ascending 1 2)
                         (assoc-in [:elevator :to-requests] [2]))
          after-state (update-elevator-state before-state)
          elevator (:elevator after-state)]
      (is (= 2 (:current-floor elevator)))
      (is (= :disembarking (:state elevator)))
      (is (= [2] (:to-requests elevator)))))

  (testing "move elevator up, reaching target, no riders, newcomers"
    (let [before-state (-> (create-state-with-defined-elevator :ascending 1 2)
                         (assoc :from-requests [{:from 2 :to 1}]))
          after-state (update-elevator-state before-state)
          elevator (:elevator after-state)]
      (is (= 2 (:current-floor elevator)))
      (is (= :embarking (:state elevator)))
      (is (= [] (:to-requests elevator)))))

  (testing "move elevator down, not reaching target"
    (let [before-state (create-state-with-defined-elevator :descending 3 1)
          after-state (update-elevator-state before-state)
          elevator (:elevator after-state)]
      (is (= 2 (:current-floor elevator)))
      (is (= :descending (:state elevator)))))

  (testing "move elevator down, reaching target, no riders, no newcomers"
    (let [before-state (create-state-with-defined-elevator :descending 2 1)
          after-state (update-elevator-state before-state)
          elevator (:elevator after-state)]
      (is (= 1 (:current-floor elevator)))
      (is (= :waiting (:state elevator)))))

  (testing "move elevator down, reaching target, riders"
    (let [before-state (-> (create-state-with-defined-elevator :descending 3 2)
                         (assoc-in [:elevator :to-requests] [2]))
          after-state (update-elevator-state before-state)
          elevator (:elevator after-state)]
      (is (= 2 (:current-floor elevator)))
      (is (= :disembarking (:state elevator)))
      (is (= [2] (:to-requests elevator)))))

  (testing "move elevator down, reaching target, no riders, newcomers"
    (let [before-state (-> (create-state-with-defined-elevator :descending 3 2)
                         (assoc :from-requests [{:from 2 :to 1}]))
          after-state (update-elevator-state before-state)
          elevator (:elevator after-state)]
      (is (= 2 (:current-floor elevator)))
      (is (= :embarking (:state elevator)))
      (is (= [] (:to-requests elevator)))))

  (testing "move waiting elevator does nothing"
    (let [before-state (create-state-with-defined-elevator :waiting 1 1)
          after-state (update-elevator-state before-state)
          elevator (:elevator after-state)]
      (is (= 1 (:current-floor elevator)))
      (is (= :waiting (:state elevator))))))

;(testing "elevator disembarking to embarking")
;(testing "elevator disembarking to waiting (no newcomers)")
;(testing "elevator embarking to waiting"))