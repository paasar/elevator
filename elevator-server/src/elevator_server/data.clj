(ns elevator-server.data
  (:require [cheshire.core :as json]))

(def number-of-floors 5)

(def total-number-of-requests 5)

(def state (atom {}))

(defn get-internal-state [] @state)

(defn set-internal-state [new-state] (reset! state new-state))

(defn generate-request [highest-floor]
  (let [current-floor (inc (rand-int highest-floor))
        highest-floor-exclusive (inc highest-floor)
        possible-floors (vec (disj (set (range 1 highest-floor-exclusive))
                                   current-floor))]
    {:from current-floor
     :to (rand-nth possible-floors)}))

(def template-content
  (json/parse-string (slurp "resources/state-template.json") true))

(defn create-floor [floor-number]
  (let [floor-template (first (template-content :floors))]
    (assoc-in floor-template [:number] floor-number)))

(defn create-floors [highest-floor]
  (map #(create-floor %) (range 1 (inc highest-floor))))

(defn set-default-floors [content]
  (assoc-in content [:floors] (create-floors number-of-floors)))

(defn clear-from-requests [content]
  (assoc-in content [:from-requests] []))

(defn create-new-state-data []
  (-> template-content
      set-default-floors
      clear-from-requests))

(defn add-next-request [cur-state]
  (assoc-in cur-state [:from-requests]
           (conj (:from-requests cur-state) (generate-request))))