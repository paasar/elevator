(ns elevator-server.view
  (:require [clojure.core.incubator :refer [dissoc-in]]
            [elevator-server.util :refer [empty-if-nil]]
            [elevator-server.constants :refer [*impatience-start* *happy-unhappy-ratio*]]))

(defn add-overall-score [tally]
  (let [happy (:happy tally)
        unhappy (:unhappy tally)]
  (assoc tally :overall (- happy (* *happy-unhappy-ratio* unhappy)))))

(defn create-elevator-for-view [elevator]
  (let [to-requests (:to-requests elevator)
        capacity (:capacity elevator)]
  {:goingTo (:going-to elevator)
   :state (:state elevator)
   :riders (into to-requests (repeat (- capacity (count to-requests)) "free"))}))

(defn floor-numbers [max-floor]
  (range max-floor 0 -1))

(defn on-off [pred]
  (if pred "on" "off"))

(defn create-button [number riders]
  (let [button-on (some #(= number %) riders)]
    {:number number :state (on-off button-on)}))

(defn create-control-panel-for-view [riders max-floor]
  (vec (map #(create-button % riders) (floor-numbers max-floor))))

(defn add-control-panel-and-elevator-if-same-floor [floor elevator max-floor]
  (if (= (:number floor) (:current-floor elevator))
    (-> floor
        (assoc :elevator (create-elevator-for-view elevator))
        (assoc :controlPanel (create-control-panel-for-view (:to-requests elevator) max-floor)))
    floor))

(defn add-counter-weight-if-correct-floor [floor max-floor elevator-floor]
  (if (= (:number floor) (inc (- max-floor elevator-floor)))
    (assoc floor :counterWeight true)
    floor))

(defn create-requests-for-view [requests]
  (vec (map #(if (< (:waited %) *impatience-start*) "patient" "impatient") requests)))

(defn create-waiting-room [current-floor requests]
  (let [any-up (some #(< current-floor (:to %)) requests)
        any-down (some #(> current-floor (:to %)) requests)]
    {:up (on-off any-up)
     :down (on-off any-down)
     :requests (create-requests-for-view requests)}))

(defn add-waiting-room [floor requests]
  (let [current-floor (:number floor)
        requests-in-this-floor (empty-if-nil (filter #(= current-floor (:from %)) requests))]
      (assoc floor :waitingRoom (create-waiting-room current-floor requests-in-this-floor))))

(defn create-floor-for-view [number elevator requests max-floor]
  (-> {:number number}
      (add-control-panel-and-elevator-if-same-floor elevator max-floor)
      (add-counter-weight-if-correct-floor max-floor (:current-floor elevator))
      (add-waiting-room requests)))

(defn create-view-floors [elevator requests max-floor]
    (vec (map #(create-floor-for-view % elevator requests max-floor) (floor-numbers max-floor))))

(defn player-state->view-data [player-key player-state]
  (let [elevator (:elevator player-state)
        max-floor (:floors player-state)
        requests (:from-requests player-state)
        tally (:tally player-state)]
    (-> {}
      (assoc :tally (add-overall-score tally))
      (assoc :floors (create-view-floors elevator requests max-floor))
      (assoc :client {:name (:name player-key)}))))

(defn sort-game-state-by-name-ip-port [game-state]
  (sort-by (fn [[key _]] ((juxt :name :ip :port) key)) game-state))

(defn game-state->view-data [state]
  (map (fn [[player-key player-state]] (player-state->view-data player-key player-state))
       (sort-game-state-by-name-ip-port state)))