(ns elevator-server.data
  (:require [cheshire.core :as json]
            [clojure.core.incubator :refer [dissoc-in]]))

(def number-of-floors 5)

(def capacity number-of-floors)

(def total-number-of-requests 5)

(def impatience-start 5)

;game-state is vector of player-states
(def game-state (atom []))

(defn get-game-state [] @game-state)

(defn set-game-state [new-state] (reset! game-state new-state))

(defn generate-request [highest-floor]
  (let [current-floor (inc (rand-int highest-floor))
        highest-floor-exclusive (inc highest-floor)
        possible-floors (vec (disj (set (range 1 highest-floor-exclusive))
                                   current-floor))]
    {:from current-floor
     :to (rand-nth possible-floors)
     :waited 0}))

(def player-state-template
  (json/parse-string (slurp "resources/player-state-template.json") true))

(defn set-floor-amount [player-state]
  (assoc-in player-state [:floors] number-of-floors))

(defn set-elevator-capacity [player-state]
  (assoc-in player-state [:elevator :capacity] capacity))

(defn get-direction [request]
  (let [from (:from request)
        to (:to request)]
    (if (< from to)
      "up"
      "down")))

(defn is-impatient? [waited]
  (>= waited impatience-start))

(defn transform-from-request-to-public [request]
  {:floor (:from request)
   :direction (get-direction request)
   :impatient (is-impatient? (:waited request))})

(defn transform-player-state-to-public [player-state]
  (-> player-state
    (dissoc-in [:client :ip])
    (dissoc-in [:client :port])
    (dissoc-in [:elevator :state])
    (dissoc-in [:elevator :going-to])
    (update-in [:from-requests] #(map transform-from-request-to-public %))))

(defn transform-game-state-to-public [internal-game-state]
  (map transform-player-state-to-public internal-game-state))

(defn clear-from-requests [player-state]
  (assoc-in player-state [:from-requests] []))

(defn create-new-player-state []
  (-> player-state-template
      set-floor-amount
      set-elevator-capacity
      clear-from-requests))

(defn add-next-request [player-state next-request]
  (assoc-in player-state
            [:from-requests]
            (conj (:from-requests player-state) next-request)))