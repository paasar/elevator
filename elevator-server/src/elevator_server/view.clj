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

(defn floor-numbers [max-floor]
  (range max-floor 0 -1))

(defn create-button [number riders]
  (let [button-on (some #(= number %) riders)]
    {(keyword (str number)) (if button-on "on" "off")}))

(defn create-control-panel-for-view [riders max-floor]
  (vec (map #(create-button % riders) (floor-numbers max-floor))))

(defn add-control-panel-and-elevator-if-same-floor [floor elevator max-floor]
  (if (= (:number floor) (:current-floor elevator))
    (-> floor
        (assoc :elevator (create-elevator-for-view elevator))
        (assoc :control-panel (create-control-panel-for-view (:to-requests elevator) max-floor)))
    floor))

(defn add-counter-weight-if-correct-floor [floor max-floor elevator-floor]
  (if (= (:number floor) (inc (- max-floor elevator-floor)))
    (assoc floor :counter-weight true)
    floor))

(defn create-floor-for-view [number elevator requests max-floor]
  (-> {:number number}
      (add-control-panel-and-elevator-if-same-floor elevator max-floor)
      (add-counter-weight-if-correct-floor max-floor (:current-floor elevator))))

(defn create-view-floors [elevator requests max-floor]
    (vec (map #(create-floor-for-view % elevator requests max-floor) (floor-numbers max-floor))))

(defn transform-player-state-to-view-data [player-state]
  (let [client (:client player-state)
        elevator (:elevator player-state)
        max-floor (:floors player-state)
        requests (:from-requests player-state)
        tally (:tally player-state)]
  (do
    (println "tpstvd" client elevator requests tally)
    (-> {}
      (assoc :tally (add-overall-score tally))
      (assoc :floors (create-view-floors elevator requests max-floor))
      (assoc :client {:name (:name client)})
      ))
  )
)

(defn transform-game-state-to-view-data [state]
  (map transform-player-state-to-view-data state))