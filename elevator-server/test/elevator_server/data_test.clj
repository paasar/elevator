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

(defn set-up-player-state-for-transformation [player-state]
  (-> player-state
    (assoc-in [:elevator :to-requests] [3 2])
    (add-next-request patient-request)
    (assoc :floors 5)))

(def expected-public-player-state
  (json/parse-string (slurp "resources/test/public-player-state.json") true))

(deftest state-manipulation
  (testing "create new player state"
    (let [new-state (create-new-player-state)]
      (is (= (:from-requests new-state) []))
      (is (= (:floors new-state) number-of-floors))))

  (testing "transform single player state into public form"
    (let [game-state (set-up-player-state-for-transformation (create-new-player-state))
          public-data (transform-player-state-to-public game-state)]
      (is (= public-data expected-public-player-state))))

  (testing "add new request"
    (let [request {:from 2 :to 3}
          state-with-request (add-next-request (create-new-player-state) request)]
      (is (= request (first (:from-requests state-with-request))))))

  (testing "transform full internal game state to public"
    (let [player-state (set-up-player-state-for-transformation (create-new-player-state))
          internal-game-state (vector player-state player-state)
          public-game-state (transform-game-state-to-public internal-game-state)
          expected-result (vector expected-public-player-state expected-public-player-state)]
      (is (= public-game-state expected-result))))

  (testing "transform patient request"
    (let [transformed-state (transform-from-request-to-public patient-request)]
      (is (= {:floor 5 :direction "down" :impatient false} transformed-state))))

  (testing "transform impatient request"
    (let [transformed-state (transform-from-request-to-public impatient-request)]
      (is (= {:floor 5 :direction "down" :impatient true} transformed-state))))

  (testing "increment wait time"
    (let [result (increment-wait-time patient-request)]
      (is (= {:from 5 :to 3 :waited 2} result)))))

(defn count-test-requests [player-state]
  (count (filter #(= (:from %) 'test) (:from-requests player-state))))

(deftest waiting-and-tally
  (testing "waited too long"
    (let [start-player-state (add-next-request (create-new-player-state)
                                               {:from 'test :to 3 :waited (- max-wait-time 2)})
          state-after-first-step (advance-player-state start-player-state)
          state-after-second-step (advance-player-state state-after-first-step)]
      (is (= 2 (count (:from-requests state-after-first-step))))
      (is (= 2 (count (:from-requests state-after-second-step))))
      (is (= 0 (count-test-requests state-after-second-step)))))

  (testing "unhappy tally is incremented"
    (let [start-player-state (add-next-request (create-new-player-state)
                                               {:from 'test :to 3 :waited max-wait-time})
          state-after-step (advance-player-state start-player-state)]
      (is (= 1 (get-in state-after-step [:tally :unhappy]))))))

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
    (let [before-state (create-new-player-state)
          after-state (set-new-target-floor before-state 2)
          elevator (get after-state :elevator)]
      (is (= 1 (:current-floor elevator)))
      (is (= 2 (:going-to elevator)))
      (is (= :ascending (:state elevator)))))

  (testing "move elevator up, not reaching target"
    (let [before-state (create-state-with-defined-elevator :ascending 1 3)
          after-state (update-elevator-state before-state)
          elevator (get after-state :elevator)]
      (is (= 2 (:current-floor elevator)))
      (is (= :ascending (:state elevator)))))

  (testing "move elevator up, reaching target, no riders, no newcomers"
    (let [before-state (create-state-with-defined-elevator :ascending 2 3)
          after-state (update-elevator-state before-state)
          elevator (get after-state :elevator)]
      (is (= 3 (:current-floor elevator)))
      (is (= :waiting (:state elevator)))))

  ;(testing "move elevator up, reaching target, riders")
  ;(testing "move elevator up, reaching target, no riders, newcomers")

  (testing "move elevator down, not reaching target"
    (let [before-state (create-state-with-defined-elevator :descending 3 1)
          after-state (update-elevator-state before-state)
          elevator (get after-state :elevator)]
      (is (= 2 (:current-floor elevator)))
      (is (= :descending (:state elevator)))))

  (testing "move elevator down, reaching target, no riders, no newcomers"
    (let [before-state (create-state-with-defined-elevator :descending 2 1)
          after-state (update-elevator-state before-state)
          elevator (get after-state :elevator)]
      (is (= 1 (:current-floor elevator)))
      (is (= :waiting (:state elevator)))))

  ;(testing "move elevator down, reaching target, riders")
  ;(testing "move elevator down, reaching target, no riders, newcomers")

  (testing "move waiting elevator does nothing"
    (let [before-state (create-state-with-defined-elevator :waiting 1 1)
          after-state (update-elevator-state before-state)
          elevator (get after-state :elevator)]
      (is (= 1 (:current-floor elevator)))
      (is (= :waiting (:state elevator))))))

  ;(testing "elevator disembarking to embarking")
  ;(testing "elevator disembarking to waiting (no newcomers)")
  ;(testing "elevator embarking to waiting"))