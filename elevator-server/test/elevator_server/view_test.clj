(ns elevator-server.view-test
  (:require [clojure.test :refer :all]
            [elevator-server.view :refer [transform-player-state-to-view-data]]
            [elevator-server.core :refer [create-new-player-state]]
            [elevator-server.constants :refer [impatience-start]]
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
                             (create-request 3 2 (inc impatience-start))])))

(def expected-data
  (json/parse-string (slurp "resources/test/view-data.json") true))

(deftest player-state-to-view-state
  (testing "transform player state"
    (let [before-state (create-player-state)
          transformed-state (transform-player-state-to-view-data before-state)]
      ;Using stupid generate-string here because elevator keystring comparing trouble
      ; eg. "ascending" vs. :ascending
      ;TODO Create custom decoder?
      (is (= (json/generate-string expected-data)
             (json/generate-string transformed-state))))))