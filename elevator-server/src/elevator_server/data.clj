(ns elevator-server.data
  (:require [cheshire.core :as json]
            [clojure.core.incubator :refer [dissoc-in]]))

(def number-of-floors 5)

(def capacity number-of-floors)

(def total-number-of-requests 5)

(def impatience-start 5)

(def max-wait-time (* 2 impatience-start))

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

(defn transform-game-state-to-public [state]
  (map transform-player-state-to-public state))

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

(defn increment-wait-time [from-request]
  (update-in from-request [:waited] inc))

(defn increment-wait-times [player-state]
  (update-in player-state [:from-requests] #(map increment-wait-time %)))

(defn remove-requests-that-have-waited-too-long-and-update-unhappy-tally [player-state]
  (let [request-groups (group-by #(< (:waited %) max-wait-time) (:from-requests player-state))
        happy-group (get request-groups true)
        unhappy-group (get request-groups false)]
    (-> player-state
      (assoc :from-requests happy-group)
      (update-in [:tally :unhappy] + (count unhappy-group)))))

(defn get-ascending-or-descending-or-waiting [current-floor target-floor]
  (cond
    (= target-floor current-floor) :waiting
    (> target-floor current-floor) :ascending
    :else :descending))

(defn set-new-target-floor [player-state target-floor]
  (let [current-floor (get-in player-state [:elevator :current-floor])]
    (-> player-state
      (assoc-in [:elevator :going-to]
                target-floor)
      (assoc-in [:elevator :state]
                (get-ascending-or-descending-or-waiting current-floor target-floor)))))

(defn advance-player-state [player-state]
  (-> player-state
    (increment-wait-times)
    (remove-requests-that-have-waited-too-long-and-update-unhappy-tally)
    (add-next-request (generate-request number-of-floors))))

(defn advance-game-state [state]
  (map advance-player-state state))