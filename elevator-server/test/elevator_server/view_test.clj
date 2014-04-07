(ns elevator-server.view-test
  (:require [clojure.test :refer :all]
            [elevator-server.view :refer [transform-player-state-to-view-data]]
            [elevator-server.core :refer [create-new-player-state]]
            [cheshire.core :as json]))

(defn create-player-state []
  (-> (create-new-player-state)))

(def expected-data
  (json/parse-string (slurp "resources/test/view-data.json") true))

(deftest player-state-to-view-state
  (testing "transform player state"
    (let [before-state (create-player-state)
          transformed-state (transform-player-state-to-view-data before-state)]
      (is (= expected-data transformed-state)))))