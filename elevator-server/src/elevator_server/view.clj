(ns elevator-server.view
  (:require [clojure.core.incubator :refer [dissoc-in]]))

(def happy-unhappy-ratio 2)

(defn add-overall-score [tally]
  (let [happy (:happy tally)
        unhappy (:unhappy tally)]
  (assoc tally :overall (- happy (* happy-unhappy-ratio unhappy)))))

(defn create-elevator-for-view [elevator]
  (let [to-requests (:to-requests elevator)
        capacity (:capacity elevator)]
  {:state (:state elevator)
   :riders (into to-requests (repeat (- capacity (count to-requests)) "free"))}))

(defn add-elevator-if-same-floor [floor elevator]
  (if (= (:number floor) (:current-floor elevator))
    (assoc floor :elevator (create-elevator-for-view elevator))
    floor))

(defn add-counter-weight-if-correct-floor [floor floors elevator-floor]
  (if (= (:number floor) (inc (- floors elevator-floor)))
    (assoc floor :counter-weight true)
    floor))

(defn create-floor-for-view [number elevator requests floors]
  (-> {:number number}
      (add-elevator-if-same-floor elevator)
      (add-counter-weight-if-correct-floor floors (:current-floor elevator))))

(defn create-view-floors [elevator requests floors]
  (let [floor-numbers (range floors 0 -1)]
    (vec (map #(create-floor-for-view % elevator requests floors) floor-numbers))
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