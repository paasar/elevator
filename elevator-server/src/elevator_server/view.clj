(ns elevator-server.view
  (:require [clojure.core.incubator :refer [dissoc-in]]))

(def happy-unhappy-ratio 2)

(defn add-overall-score [tally]
  (let [happy (:happy tally)
        unhappy (:unhappy tally)]
  (assoc tally :overall (- happy (* happy-unhappy-ratio unhappy)))))

(defn create-view-floor [number]
  {:number number})

(defn create-view-floors [elevator requests floors]
  (let [floor-numbers (range floors 0 -1)]
    (vec (map #(create-view-floor %) floor-numbers))
  ))

(defn transform-player-state-to-view-data [player-state]
  (let [client (:client player-state)
        elevator (:elevator player-state)
        floors (:floors player-state)
        requests (:from-requests player-state)
        tally (:tally player-state)]
  (do
    (println "tpstvd" client elevator requests tally)
    (-> {}
      (assoc :tally (add-overall-score tally))
      (assoc :floors (create-view-floors elevator requests floors))
      (assoc :client {:name (:name client)})
      ))
  )
)

(defn transform-game-state-to-view-data [state]
  (map transform-player-state-to-view-data state))