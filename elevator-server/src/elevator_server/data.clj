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
     :to (rand-nth possible-floors)
     :waited 0}))

(def state-template
  (json/parse-string (slurp "resources/state-template.json") true))

(defn set-floor-amount [state-data]
  (assoc-in state-data [:floors] number-of-floors))

(defn get-direction [request]
  (let [from (:from request)
        to (:to request)]
    (if (< from to)
      "up"
      "down")))

(defn is-impatient? [waited]
  (>= waited impatience-start))

(defn transform-from-request-to-public [request]
  {:current-floor (:from request)
   :direction (get-direction request)
   :impatient (is-impatient? (:waited request))})

(defn transform-state-to-public [state-data]
  (-> state-data
    (dissoc :client :tally)
    (dissoc-in [:elevator :state])
    (update-in [:from-requests] #(map transform-from-request-to-public %))))

(defn transform-internal-state-to-public [internal-state]
  (map transform-state-to-public internal-state))

(defn clear-from-requests [state-data]
  (assoc-in state-data [:from-requests] []))

(defn create-new-state-data []
  (-> state-template
      set-floor-amount
      clear-from-requests))

(defn add-next-request [state-data next-request]
  (assoc-in state-data
            [:from-requests]
            (conj (:from-requests state-data) next-request)))