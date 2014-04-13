(ns elevator-server.core
  (:require [cheshire.core :as json]
            [clojure.core.incubator :refer [dissoc-in]]
            [elevator-server.elevator-state :refer [update-elevator-state]]
            [elevator-server.util :refer [empty-if-nil]]))

(def number-of-floors 5)

(def capacity number-of-floors)

(def impatience-start 5)

(def max-wait-time (* 2 impatience-start))

;game-state is a vector of player-states
(def game-state (atom []))

(defn get-game-state [] @game-state)

(defn set-game-state [new-state] (reset! game-state new-state))

(defn update-game-state [update-fun] (swap! game-state update-fun))

(defn generate-request [highest-floor]
  (let [current-floor (inc (rand-int highest-floor))
        highest-floor-plus-one (inc highest-floor)
        possible-floors (vec (disj (set (range 1 highest-floor-plus-one))
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
        happy-group (empty-if-nil (get request-groups true))
        unhappy-group (empty-if-nil (get request-groups false))]
    (-> player-state
      (assoc :from-requests happy-group)
      (update-in [:tally :unhappy] + (count unhappy-group)))))

(defn advance-player-state [player-state new-request]
  (-> player-state
    (increment-wait-times)
    (remove-requests-that-have-waited-too-long-and-update-unhappy-tally)
    (update-elevator-state)
    (add-next-request new-request)))

(defn advance-game-state [state]
  (let [new-from-request (generate-request number-of-floors)]
    (map #(advance-player-state % new-from-request) state)))