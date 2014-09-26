(ns elevator-server.elevator-state-test
  (:require [clojure.test :refer :all]
            [elevator-server.elevator-state :refer :all]
            [elevator-server.core :refer [create-new-player-state]]))

(defn create-state-with-defined-elevator [state current-floor target-floor]
  (-> (create-new-player-state)
      (assoc-in [:elevator :state] state)
      (assoc-in [:elevator :current-floor] current-floor)
      (assoc-in [:elevator :going-to] target-floor)
      (assoc-in [:elevator :to-requests] [])))

(deftest basic-states
  (testing "target is current floor"
    (is (= :embarking (get-next-elevator-state 1 1 :embarking false)))
    (is (= :disembarking (get-next-elevator-state 1 1 :embarking true)))
    (is (= :embarking (get-next-elevator-state 1 1 :disembarking false)))
    (is (= :embarking (get-next-elevator-state 1 1 :disembarking true)))
    (is (= :embarking (get-next-elevator-state 1 1 :ascending false)))
    (is (= :disembarking (get-next-elevator-state 1 1 :ascending true)))
    (is (= :embarking (get-next-elevator-state 1 1 :descending false)))
    (is (= :disembarking (get-next-elevator-state 1 1 :descending true))))
  (testing "target is on lower floor"
    (is (= :descending (get-next-elevator-state 2 1 :embarking false)))
    (is (= :descending (get-next-elevator-state 2 1 :embarking true)))
    (is (= :descending (get-next-elevator-state 2 1 :disembarking false)))
    (is (= :embarking (get-next-elevator-state 2 1 :disembarking true)))
    (is (= :descending (get-next-elevator-state 2 1 :descending false)))
    (is (= :descending (get-next-elevator-state 2 1 :descending true)))
    (is (= :embarking (get-next-elevator-state 2 1 :ascending false)))
    (is (= :disembarking (get-next-elevator-state 2 1 :ascending true))))
  (testing "target is on higher floor"
    (is (= :ascending (get-next-elevator-state 1 2 :embarking false)))
    (is (= :ascending (get-next-elevator-state 1 2 :embarking true)))
    (is (= :ascending (get-next-elevator-state 1 2 :disembarking false)))
    (is (= :embarking (get-next-elevator-state 1 2 :disembarking true)))
    (is (= :embarking (get-next-elevator-state 1 2 :descending false)))
    (is (= :disembarking (get-next-elevator-state 1 2 :descending true)))
    (is (= :ascending (get-next-elevator-state 1 2 :ascending false)))
    (is (= :ascending (get-next-elevator-state 1 2 :ascending true)))))

(deftest ascending
  (testing "move elevator up, not reaching target"
    (let [before-update (create-state-with-defined-elevator :ascending 1 3)
          after-update (update-elevator-state before-update)
          elevator (:elevator after-update)]
      (is (= 2 (:current-floor elevator)))
      (is (= :ascending (get-state-as-keyword elevator)))))

  (testing "move elevator up, reaching target, no riders, no newcomers"
    (let [before-update (create-state-with-defined-elevator :ascending 2 3)
          after-update (update-elevator-state before-update)
          elevator (:elevator after-update)]
      (is (= 3 (:current-floor elevator)))
      (is (= :embarking (get-state-as-keyword elevator)))))

  (testing "move elevator up, reaching target, riders"
    (let [before-update (-> (create-state-with-defined-elevator :ascending 1 2)
                            (assoc-in [:elevator :to-requests] [2]))
          after-update (update-elevator-state before-update)
          elevator (:elevator after-update)]
      (is (= 2 (:current-floor elevator)))
      (is (= :disembarking (get-state-as-keyword elevator)))
      (is (= [2] (:to-requests elevator)))))

  (testing "move elevator up, reaching target, no riders, newcomers"
    (let [before-update (-> (create-state-with-defined-elevator :ascending 1 2)
                            (assoc :from-requests [{:from 2 :to 1}]))
          after-update (update-elevator-state before-update)
          elevator (:elevator after-update)]
      (is (= 2 (:current-floor elevator)))
      (is (= :embarking (get-state-as-keyword elevator)))
      (is (= [] (:to-requests elevator))))))

(deftest descending
  (testing "move elevator down, not reaching target"
    (let [before-update (create-state-with-defined-elevator :descending 3 1)
          after-update (update-elevator-state before-update)
          elevator (:elevator after-update)]
      (is (= 2 (:current-floor elevator)))
      (is (= :descending (get-state-as-keyword elevator)))))

  (testing "move elevator down, reaching target, no riders, no newcomers"
    (let [before-update (create-state-with-defined-elevator :descending 2 1)
          after-update (update-elevator-state before-update)
          elevator (:elevator after-update)]
      (is (= 1 (:current-floor elevator)))
      (is (= :embarking (get-state-as-keyword elevator)))))

  (testing "move elevator down, reaching target, riders"
    (let [before-update (-> (create-state-with-defined-elevator :descending 3 2)
                            (assoc-in [:elevator :to-requests] [2]))
          after-update (update-elevator-state before-update)
          elevator (:elevator after-update)]
      (is (= 2 (:current-floor elevator)))
      (is (= :disembarking (get-state-as-keyword elevator)))
      (is (= [2] (:to-requests elevator)))))

  (testing "move elevator down, reaching target, no riders, newcomers"
    (let [before-update (-> (create-state-with-defined-elevator :descending 3 2)
                            (assoc :from-requests [{:from 2 :to 1}]))
          after-update (update-elevator-state before-update)
          elevator (:elevator after-update)]
      (is (= 2 (:current-floor elevator)))
      (is (= :embarking (get-state-as-keyword elevator)))
      (is (= [] (:to-requests elevator))))))

(deftest waiting-aka-embarking
  (testing "move embarking elevator does nothing"
    (let [before-update (create-state-with-defined-elevator :embarking 1 1)
          after-update (update-elevator-state before-update)
          elevator (:elevator after-update)]
      (is (= 1 (:current-floor elevator)))
      (is (= :embarking (get-state-as-keyword elevator))))))

(deftest disembarking
  (testing "elevator disembarking to embarking"
    (let [before-update (-> (create-state-with-defined-elevator :disembarking 2 2)
                            (assoc-in [:elevator :to-requests] [2])
                            (assoc :from-requests [{:from 3 :to 1}]))
          after-update (update-elevator-state before-update)
          elevator (:elevator after-update)]
      (is (= 2 (:current-floor elevator)))
      (is (= :embarking (get-state-as-keyword elevator)))
      (is (empty? (:to-requests elevator)))
      (is (= 1 (count (:from-requests after-update))))
      (is (= 1 (get-in after-update [:tally :happy])))))

  (testing "elevator disembarking to embarking (no newcomers)"
    (let [before-update (-> (create-state-with-defined-elevator :disembarking 2 2)
                            (assoc-in [:elevator :to-requests] [2]))
          after-update (update-elevator-state before-update)
          elevator (:elevator after-update)]
      (is (= 2 (:current-floor elevator)))
      (is (= :embarking (get-state-as-keyword elevator)))
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
  (testing "elevator embarking to embarking"
    (let [before-update (-> (create-state-with-defined-elevator :embarking 2 2)
                            (assoc :from-requests [{:from 2 :to 1 :waited 0}]))
          after-update (update-elevator-state before-update)
          elevator (:elevator after-update)]
      (is (= 2 (:current-floor elevator)))
      (is (= :embarking (get-state-as-keyword elevator)))
      (is (= 1 (count (:to-requests elevator))))
      (is (empty? (:from-requests after-update)))))

  (testing "embarking appends to elevator people and doesn't remove other floor requests")
    (let [before-update (-> (create-state-with-defined-elevator :embarking 2 2)
                            (assoc-in [:elevator :to-requests] [3])
                            (assoc :from-requests [{:from 2 :to 1 :waited 0}
                                                   {:from 3 :to 4 :waited 0}]))
          after-update (update-elevator-state before-update)
          elevator (:elevator after-update)]
      (is (= [3 1] (:to-requests elevator)))
      (is (= 1 (count (:from-requests after-update)))))

  (testing "embarking adds new rider only up to capacity"
    (let [before-update (-> (create-state-with-defined-elevator :embarking 2 2)
                            (assoc-in [:elevator :to-requests] [3 3 3 3 3])
                            (assoc :from-requests [{:from 2 :to 1 :waited 0}
                                                   {:from 2 :to 4 :waited 0}]))
          after-update (update-elevator-state before-update)
          elevator (:elevator after-update)]
      (is (= [3 3 3 3 3 1] (:to-requests elevator)))
      (is (= [{:from 2 :to 4 :waited 0}] (:from-requests after-update)))))

  (testing "embark first those who have waited more"
    (let [before-update (-> (create-state-with-defined-elevator :embarking 2 2)
                            (assoc-in [:elevator :to-requests] [3 3 3 3])
                            (assoc :from-requests [{:from 2 :to 1 :waited 0}
                                                   {:from 2 :to 4 :waited 50}
                                                   {:from 2 :to 3 :waited 1}
                                                   {:from 2 :to 5 :waited 2}]))
          after-update (update-elevator-state before-update)
          elevator (:elevator after-update)]
      (is (= [3 3 3 3 4 5] (:to-requests elevator)))
      (is (= [{:from 2 :to 3 :waited 1} {:from 2 :to 1 :waited 0}] (:from-requests after-update)))))
  )