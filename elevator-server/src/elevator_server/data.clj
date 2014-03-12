(ns elevator-server.data
  (:require [cheshire.core :as json]))

(def state (atom {}))

(defn get-state [] @state)

(defn set-state [new-state] (reset! state new-state))

(def template-content
  (json/parse-string (slurp "resources/state-template.json") true))

(defn create-floor [floor-number]
  (let [floor-template (first (template-content :floors))]
    (assoc-in floor-template [:number] floor-number)))

(defn create-floors [number-of-floors]
  (map #(create-floor %) (range 1 (inc number-of-floors))))

(defn create-new-state-data []
  (assoc-in template-content [:floors] (create-floors 9)))

(defn add-first-floor-request [cur-state]
  (assoc-in cur-state [:requests] (conj (:requests cur-state) {:current-floor 1
                                                               :direction "up"})))
