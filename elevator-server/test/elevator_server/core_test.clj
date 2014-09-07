(ns elevator-server.core-test
  (:require [clojure.test :refer :all]
            [elevator-server.core :refer :all]
            [elevator-server.constants :refer [*number-of-floors* *max-wait-time*]]
            [cheshire.core :as json]))

(def patient-request {:from 5 :to 3 :waited 1})
(def impatient-request {:from 5 :to 3 :waited 11})

(def player-key-1 {:name "team-1" :ip "127.0.0.1" :port "3333"})
(def player-key-2 {:name "team-2" :ip "127.0.0.2" :port "3334"})

(defn set-up-player-state-for-transformation [player-state]
  (-> player-state
    (assoc-in [:elevator :to-requests] [3 2])
    (add-requests [patient-request])
    (assoc :floors 5)))

(defn expected-public-player-state [& {:keys [name], :or {name "team-1"}}]
  (-> (json/parse-string (slurp "resources/test/public-player-state.json") true)
    (assoc-in [:client :name] name)))

(defn keys-contain-name? [coll name]
  ((complement nil?) (some #(= name %) (map :name (keys coll)))))

(deftest state-manipulation
  (testing "create new player state"
    (let [new-state (create-new-player-state)]
      (is (= (:from-requests new-state) []))
      (is (= (:floors new-state) *number-of-floors*))))

  (testing "transform single player state into public form"
    (let [player-state (set-up-player-state-for-transformation (create-new-player-state))
          public-data (transform-player-state-to-public player-key-1 player-state)]
      (is (= public-data (expected-public-player-state)))))

  (testing "add new request"
    (let [request {:from 2 :to 3}
          state-with-request (add-requests (create-new-player-state) [request])]
      (is (= request (first (:from-requests state-with-request))))))

  (testing "transform full internal game state to public"
    (let [player-state (set-up-player-state-for-transformation (create-new-player-state))
          internal-game-state {player-key-1 player-state
                               player-key-2 player-state}
          public-game-state (transform-game-state-to-public internal-game-state)
          expected-result (vector (expected-public-player-state) (expected-public-player-state :name "team-2"))]
      (is (= public-game-state expected-result))))

  (testing "transform patient request"
    (let [transformed-state (transform-from-request-to-public patient-request)]
      (is (= {:floor 5 :direction "DOWN" :impatient false} transformed-state))))

  (testing "transform impatient request"
    (let [transformed-state (transform-from-request-to-public impatient-request)]
      (is (= {:floor 5 :direction "DOWN" :impatient true} transformed-state))))

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
                                           [{:from 'test :to 3 :waited (- *max-wait-time* 2)}])
          state-after-first-step (advance-player-state {} start-player-state [patient-request])
          state-after-second-step (advance-player-state {} state-after-first-step [patient-request])]
      (is (= 2 (count (:from-requests state-after-first-step))))
      (is (= 2 (count (:from-requests state-after-second-step))))
      (is (= 0 (count-test-requests state-after-second-step)))))

  (testing "unhappy tally is incremented"
    (let [start-player-state (add-requests (create-new-player-state)
                                           [{:from 'test :to 3 :waited *max-wait-time*}])
          state-after-step (advance-player-state {} start-player-state [patient-request])]
      (is (= 1 (get-in state-after-step [:tally :unhappy]))))))

(deftest adding-requests
  (testing "advancing state adds same request to all player states"
    (let [game-state-before {{"client" 1} (create-new-player-state)
                             {"client" 2} (create-new-player-state)}
          game-state-after (advance-game-state game-state-before)
          player-states-after (vals game-state-after)]
      (is (= (:from-requests (first player-states-after)) (:from-requests (second player-states-after)))))))

(deftest running-switch
  (testing "state should not advance if running is false"
    (let [game-state-before (create-new-player (:name player-key-1) (:ip player-key-1) (:port player-key-1))
          _ (stop-game)
          game-state-after (advance-game-state game-state-before)]
      (is (= game-state-before game-state-after)))))

(deftest player-manipulation
  (testing "create a new player with given information retains that information"
    (let [name "A-team"
          ip "10.0.0.1"
          port "4444"
          created-player-state (create-new-player name ip port)
          key (first (keys created-player-state))]
      (is (= name (:name key)))
      (is (= ip (:ip key)))
      (is (= port (:port key)))))

  (testing "deleting a player"
    (let [ip-a "10.0.0.1"
          default-port "3333"
          team-a "a"
          team-b "b"
          team-a2 "a2"
          game-state-before (-> {}
                              (create-and-add-player team-a ip-a default-port)
                              (create-and-add-player team-b "10.0.0.2" default-port)
                              (create-and-add-player team-a2 ip-a "3334"))
          game-state-after (delete-player-by-ip-and-port game-state-before ip-a default-port)]
      (is (= 2 (count game-state-after)))
      (is (= false (keys-contain-name? game-state-after team-a)))
      (is (= true (keys-contain-name? game-state-after team-b)))
      (is (= true (keys-contain-name? game-state-after team-a2))))))
