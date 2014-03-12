(ns elevator-server.data
  (:require [cheshire.core :as json]))

(def state (atom {}))

(defn get-state [] @state)

(defn set-state [new-state] (reset! state new-state))

(def default-number-of-floors 9)

(def template-content
  (json/parse-string (slurp "resources/state-template.json") true))

(defn create-floor [floor-number]
  (let [floor-template (first (template-content :floors))]
    (assoc-in floor-template [:number] floor-number)))

(defn create-floors [number-of-floors]
  (map #(create-floor %) (range 1 (inc number-of-floors))))

(defn set-default-floors [content]
  (assoc-in content [:floors] (create-floors default-number-of-floors)))

(defn clear-from-requests [content]
  (assoc-in content [:from-requests] []))

(defn create-new-state-data []
  (-> template-content
      set-default-floors
      clear-from-requests))

;TODO sane functionality
(defn add-first-floor-request [cur-state]
  (assoc-in cur-state
            [:from-requests]
            (conj (:from-requests cur-state) {:current-floor 1
                                              :direction "up"})))
