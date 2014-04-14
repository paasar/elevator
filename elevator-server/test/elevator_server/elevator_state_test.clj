(ns elevator-server.elevator-state-test
  (:require [clojure.test :refer :all]
            [elevator-server.elevator-state :refer :all]
            [elevator-server.core :refer [create-new-player-state]]))

(defn create-state-with-defined-elevator [state current-floor target-floor]
  (-> (create-new-player-state)
    (assoc-in [:elevator :state] state)
    (assoc-in [:elevator :current-floor] current-floor)
    (assoc-in [:elevator :going-to] target-floor)))

(deftest basic-states
  (testing "same floor gives waiting"
    (is (= :waiting (get-next-elevator-state 1 1 :embarking false false)))
    (is (= :waiting (get-next-elevator-state 1 1 :disembarking false false)))
    (is (= :waiting (get-next-elevator-state 1 1 :waiting false false))))
  (testing "higher target floor gives ascending"
    (is (= :ascending (get-next-elevator-state 1 2 :ascending false false))))
  (testing "lower target floor gives descending"
    (is (= :descending (get-next-elevator-state 2 1 :descending false false))))
  (testing "disembarking with combinatons of riders and newcomers"
    (is (= :embarking (get-next-elevator-state 1 1 :disembarking true true)))
    (is (= :embarking (get-next-elevator-state 1 1 :disembarking false true)))
    (is (= :waiting (get-next-elevator-state 1 1 :disembarking true false)))
    (is (= :embarking (get-next-elevator-state 1 2 :disembarking true true)))
    (is (= :ascending (get-next-elevator-state 1 2 :disembarking true false)))
    (is (= :descending (get-next-elevator-state 2 1 :disembarking true false))))
  (testing "embarking with combinatons of riders and newcomers"
    (is (= :waiting (get-next-elevator-state 1 1 :embarking true true)))
    (is (= :waiting (get-next-elevator-state 1 1 :embarking false true)))
    (is (= :waiting (get-next-elevator-state 1 1 :embarking true false))))

  (testing "setting elevator to go up"
    (let [before-update (create-new-player-state)
          after-update (set-new-target-floor before-update 2)
          elevator (:elevator after-update)]
      (is (= 1 (:current-floor elevator)))
      (is (= 2 (:going-to elevator)))
      (is (= :ascending (:state elevator)))))

  (testing "setting elevator to go down"
    (let [before-update (create-state-with-defined-elevator :waiting 2 2)
          after-update (set-new-target-floor before-update 1)
          elevator (:elevator after-update)]
      (is (= 2 (:current-floor elevator)))
      (is (= 1 (:going-to elevator)))
      (is (= :descending (:state elevator))))))

(deftest ascending
  (testing "move elevator up, not reaching target"
    (let [before-update (create-state-with-defined-elevator :ascending 1 3)
          after-update (update-elevator-state before-update)
          elevator (:elevator after-update)]
      (is (= 2 (:current-floor elevator)))
      (is (= :ascending (:state elevator)))))

  (testing "move elevator up, reaching target, no riders, no newcomers"
    (let [before-update (create-state-with-defined-elevator :ascending 2 3)
          after-update (update-elevator-state before-update)
          elevator (:elevator after-update)]
      (is (= 3 (:current-floor elevator)))
      (is (= :waiting (:state elevator)))))

  (testing "move elevator up, reaching target, riders"
    (let [before-update (-> (create-state-with-defined-elevator :ascending 1 2)
                            (assoc-in [:elevator :to-requests] [2]))
          after-update (update-elevator-state before-update)
          elevator (:elevator after-update)]
      (is (= 2 (:current-floor elevator)))
      (is (= :disembarking (:state elevator)))
      (is (= [2] (:to-requests elevator)))))

  (testing "move elevator up, reaching target, no riders, newcomers"
    (let [before-update (-> (create-state-with-defined-elevator :ascending 1 2)
                            (assoc :from-requests [{:from 2 :to 1}]))
          after-update (update-elevator-state before-update)
          elevator (:elevator after-update)]
      (is (= 2 (:current-floor elevator)))
      (is (= :embarking (:state elevator)))
      (is (= [] (:to-requests elevator))))))

(deftest descending
  (testing "move elevator down, not reaching target"
    (let [before-update (create-state-with-defined-elevator :descending 3 1)
          after-update (update-elevator-state before-update)
          elevator (:elevator after-update)]
      (is (= 2 (:current-floor elevator)))
      (is (= :descending (:state elevator)))))

  (testing "move elevator down, reaching target, no riders, no newcomers"
    (let [before-update (create-state-with-defined-elevator :descending 2 1)
          after-update (update-elevator-state before-update)
          elevator (:elevator after-update)]
      (is (= 1 (:current-floor elevator)))
      (is (= :waiting (:state elevator)))))

  (testing "move elevator down, reaching target, riders"
    (let [before-update (-> (create-state-with-defined-elevator :descending 3 2)
                            (assoc-in [:elevator :to-requests] [2]))
          after-update (update-elevator-state before-update)
          elevator (:elevator after-update)]
      (is (= 2 (:current-floor elevator)))
      (is (= :disembarking (:state elevator)))
      (is (= [2] (:to-requests elevator)))))

  (testing "move elevator down, reaching target, no riders, newcomers"
    (let [before-update (-> (create-state-with-defined-elevator :descending 3 2)
                            (assoc :from-requests [{:from 2 :to 1}]))
          after-update (update-elevator-state before-update)
          elevator (:elevator after-update)]
      (is (= 2 (:current-floor elevator)))
      (is (= :embarking (:state elevator)))
      (is (= [] (:to-requests elevator))))))

(deftest waiting
  (testing "move waiting elevator does nothing"
    (let [before-update (create-state-with-defined-elevator :waiting 1 1)
          after-update (update-elevator-state before-update)
          elevator (:elevator after-update)]
      (is (= 1 (:current-floor elevator)))
      (is (= :waiting (:state elevator))))))

(deftest disembarking
  (testing "elevator disembarking to embarking"
    (let [before-update (-> (create-state-with-defined-elevator :disembarking 2 2)
                            (assoc-in [:elevator :to-requests] [2])
                            (assoc :from-requests [{:from 3 :to 1}]))
          after-update (update-elevator-state before-update)
          elevator (:elevator after-update)]
      (is (= 2 (:current-floor elevator)))
      (is (= :embarking (:state elevator)))
      (is (empty? (:to-requests elevator)))
      (is (= 1 (count (:from-requests after-update))))
      (is (= 1 (get-in after-update [:tally :happy])))))

  (testing "elevator disembarking to waiting (no newcomers)"
    (let [before-update (-> (create-state-with-defined-elevator :disembarking 2 2)
                            (assoc-in [:elevator :to-requests] [2]))
          after-update (update-elevator-state before-update)
          elevator (:elevator after-update)]
      (is (= 2 (:current-floor elevator)))
      (is (= :waiting (:state elevator)))
      (is (empty? (:to-requests elevator)))
      (is (= 1 (get-in after-update [:tally :happy])))))

  (testing "disembarking leaves other people in elevator and doesn't affect other floor requests"
    (let [before-update (-> (create-state-with-defined-elevator :disembarking 2 2)
                            (assoc-in [:elevator :to-requests] [2 3])
                            (assoc :from-requests [{:from 3 :to 1}]))
          after-update (update-elevator-state before-update)
          elevator (:elevator after-update)]
      (is (= [3] (:to-requests elevator)))
      (is (= 1 (count (:from-requests after-update)))))))

(deftest embarking
  (testing "elevator embarking to waiting"
    (let [before-update (-> (create-state-with-defined-elevator :embarking 2 2)
                            (assoc :from-requests [{:from 2 :to 1}]))
          after-update (update-elevator-state before-update)
          elevator (:elevator after-update)]
      (is (= 2 (:current-floor elevator)))
      (is (= :waiting (:state elevator)))
      (is (= 1 (count (:to-requests elevator))))
      (is (empty? (:from-requests after-update)))))

  (testing "embarking appends to elevator people and doesn't remove other floor requests")
    (let [before-update (-> (create-state-with-defined-elevator :embarking 2 2)
                            (assoc-in [:elevator :to-requests] [3])
                            (assoc :from-requests [{:from 2 :to 1} {:from 3 :to 4}]))
          after-update (update-elevator-state before-update)
          elevator (:elevator after-update)]
      (is (= [3 1] (:to-requests elevator)))
      (is (= 1 (count (:from-requests after-update)))))

  (testing "embarking adds new rider only up to capacity"
    (let [before-update (-> (create-state-with-defined-elevator :embarking 2 2)
                            (assoc-in [:elevator :to-requests] [3 3 3 3])
                            (assoc :from-requests [{:from 2 :to 1} {:from 2 :to 4}]))
          after-update (update-elevator-state before-update)
          elevator (:elevator after-update)]
      (is (= [3 3 3 3 1] (:to-requests elevator)))
      (is (= [{:from 2 :to 4}] (:from-requests after-update)))))

  ;TODO impatients embark first
  )