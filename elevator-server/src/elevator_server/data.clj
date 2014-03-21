(ns elevator-server.data
  (:require [cheshire.core :as json]
            [clojure.core.incubator :refer [dissoc-in]]))

(def number-of-floors 5)

(def total-number-of-requests 5)

(def impatience-start 5)

(def state (atom []))

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

(defn get-normal-waiters [waiting impatience-start]
  (count (filter #(< % impatience-start) waiting)))

(defn get-impatient-waiters [waiting impatience-start]
  (count (filter #(>= % impatience-start) waiting)))

(defn transform-floor-to-public [internal-floor impatientence-start]
  (let [waiting (:waiting internal-floor)]
    {:number (:number internal-floor)
     :waiting (get-normal-waiters waiting impatientence-start)
     :impatient (get-impatient-waiters waiting impatientence-start)}))

(defn get-direction [request]
  (let [from (:from request)
        to (:to request)]
    (if (< from to)
      "up"
      "down")))

(defn transform-from-request-to-public [request]
  {:current-floor (:from request)
   :direction (get-direction request)})

(defn transform-state-to-public [state-data]
  (-> state-data
    (dissoc :client :tally)
    (dissoc-in [:elevator :state])
    (update-in [:from-requests] #(map transform-from-request-to-public %))
    (update-in [:floors] #(map (fn [floor] (transform-floor-to-public floor impatience-start)) %))))

(defn transform-internal-state-to-public [internal-state]
  (map transform-state-to-public internal-state))

(defn clear-from-requests [state-data]
  (assoc-in state-data [:from-requests] []))

(defn create-new-state-data []
  (-> template-content
      set-default-floors
      clear-from-requests))

(defn add-next-request [state-data next-request]
  (assoc-in state-data
            [:from-requests]
            (conj (:from-requests state-data) next-request)))