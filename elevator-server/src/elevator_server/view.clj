(ns elevator-server.view
  (:require [clojure.core.incubator :refer [dissoc-in]]))

(defn add-overall-score [tally]
  (let [happy (:happy tally)
        unhappy (:unhappy tally)]
  (assoc tally :overall (- happy (* 2 unhappy)))))

(defn create-view-floors [elevator requests]
  {};TODO
  )

(defn transform-player-state-to-view-data [player-state]
  (let [client (:client player-state)
        elevator (:elevator player-state)
        requests (:requests player-state)
        tally (:tally player-state)]
  (do
    (println client elevator requests tally)
    (-> {}
      (assoc :tally (add-overall-score tally))
      (assoc :floors (create-view-floors elevator requests))
      (assoc :client {:name (:name client)})
      ))
  )
)

(defn transform-game-state-to-view-data [state]
  (map transform-player-state-to-view-data state))