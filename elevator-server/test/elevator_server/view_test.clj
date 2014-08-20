(ns elevator-server.view-test
  (:require [clojure.test :refer :all]
            [elevator-server.view :refer [player-state->view-data sort-game-state-by-name-ip-port]]
            [elevator-server.core :refer [create-new-player-state]]
            [elevator-server.constants :refer [*impatience-start*]]
            [cheshire.core :as json]))

(defn create-request [from to waited]
  {:from from :to to :waited waited})

(defn create-player-state []
  (-> (create-new-player-state)
      (assoc :tally {:happy 3 :unhappy 1})
      (assoc :elevator {:to-requests [5 4]
                        :current-floor 3
                        :going-to 4
                        :state :ascending
                        :capacity 5})
      (assoc :from-requests [(create-request 1 2 1)
                             (create-request 3 2 1)
                             (create-request 3 2 (inc *impatience-start*))])))

(def expected-data
  (json/parse-string (slurp "resources/test/view-data.json") true))

(deftest player-state-to-view-state
  (testing "transform player state"
    (let [player-key {:name "team-1"
                      :ip "127.0.0.1"
                      :port "3333"}
          before-state (create-player-state)
          transformed-state (player-state->view-data player-key before-state)]
      ;Using stupid generate-string here because elevator keystring comparing trouble
      ; eg. "ascending" vs. :ascending
      ;TODO Create custom decoder?
      (is (= (json/generate-string expected-data)
             (json/generate-string transformed-state))))))

(deftest game-state-sorting
  (testing "states are in order"
    (let [a3 {:name "team-a" :ip "127.0.0.1" :port "3333"}
          a4 {:name "team-a" :ip "127.0.0.1" :port "3334"}
          b1 {:name "team-b" :ip "127.0.0.1" :port "3333"}
          b2 {:name "team-b" :ip "127.0.0.2" :port "3333"}
           before-state {b2 (create-player-state)
                        a4 (create-player-state)
                        b1 (create-player-state)
                        a3 (create-player-state)}
          after-state (sort-game-state-by-name-ip-port before-state)
          keys (keys after-state)]
      (is (= a3 (nth keys 0)))
      (is (= a4 (nth keys 1)))
      (is (= b1 (nth keys 2)))
      (is (= b2 (nth keys 3))))))