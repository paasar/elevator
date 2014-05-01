(ns elevator-server.core-test
  (:require [clojure.test :refer :all]
            [elevator-server.core :refer :all]
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
    (add-requests [patient-request])
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
          state-with-request (add-requests (create-new-player-state) [request])]
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
      (is (= {:from 5 :to 3 :waited 2} result))))

  (testing "increment tick"
    (let [result (increment-tick (create-new-player-state))]
      (is (= 1 (:tick result))))))

(defn count-test-requests [player-state]
  (count (filter #(= (:from %) 'test) (:from-requests player-state))))

(deftest waiting-and-tally
  (testing "waited too long"
    (let [start-player-state (add-requests (create-new-player-state)
                                           [{:from 'test :to 3 :waited (- max-wait-time 2)}])
          state-after-first-step (advance-player-state start-player-state [patient-request])
          state-after-second-step (advance-player-state state-after-first-step [patient-request])]
      (is (= 2 (count (:from-requests state-after-first-step))))
      (is (= 2 (count (:from-requests state-after-second-step))))
      (is (= 0 (count-test-requests state-after-second-step)))))

  (testing "unhappy tally is incremented"
    (let [start-player-state (add-requests (create-new-player-state)
                                           [{:from 'test :to 3 :waited max-wait-time}])
          state-after-step (advance-player-state start-player-state [patient-request])]
      (is (= 1 (get-in state-after-step [:tally :unhappy]))))))

(deftest adding-requests
  (testing "advancing state adds same request to all player states"
    (let [game-state-before (vec (repeat 2 (create-new-player-state)))
          game-state-after (advance-game-state game-state-before)]
      (is (= (:from-requests (first game-state-after)) (:from-requests (second game-state-after)))))))