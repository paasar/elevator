(ns elevator-server.core
  (:require [cheshire.core :as json]
            [clojure.core.incubator :refer [dissoc-in]]
            [elevator-server.elevator-state :refer [update-elevator-state]]
            [elevator-server.util :refer [empty-if-nil]]
            [elevator-server.request-generator :refer [generate-requests]]
            [elevator-server.constants :refer [*number-of-floors* *capacity* *impatience-start* *max-wait-time*]]))

;game-state is a vector of player-states
(def game-state (atom []))

(defn get-game-state [] @game-state)

(defn set-game-state [new-state] (reset! game-state new-state))

(defn update-game-state [update-fun] (swap! game-state update-fun))

(def player-state-template
  (json/parse-string (slurp "resources/player-state-template.json") true))

(def running (atom true))

(defn stop-game [] (reset! running false))

(defn run-game [] (reset! running true))

(defn running? [] @running)

(defn set-floor-amount [player-state]
  (assoc-in player-state [:floors] *number-of-floors*))

(defn set-elevator-capacity [player-state]
  (assoc-in player-state [:elevator :capacity] *capacity*))

(defn get-direction [request]
  (let [from (:from request)
        to (:to request)]
    (if (< from to)
      "up"
      "down")))

(defn is-impatient? [waited]
  (>= waited *impatience-start*))

(defn transform-from-request-to-public [request]
  {:floor (:from request)
   :direction (get-direction request)
   :impatient (is-impatient? (:waited request))})

(defn transform-player-state-to-public [player-state]
  (-> player-state
    (dissoc-in [:client :ip])
    (dissoc-in [:client :port])
    (dissoc-in [:tick])
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

(defn create-new-player [name ip port]
  (-> (create-new-player-state)
      (assoc-in [:client :name] name)
      (assoc-in [:client :ip] ip)
      (assoc-in [:client :port] port)))

;TODO handle error cases like duplicate name or ip
(defn create-and-add-player [cur-game-state name ip port]
  (let [new-player-state (create-new-player name ip port)
        game-state-with-player-added (conj cur-game-state new-player-state)]
    game-state-with-player-added))

;TODO handle error cases like not found with ip
(defn delete-player [cur-game-state ip]
  (let [game-state-after-player-removal (filter #(not= ip (get-in % [:client :ip])) cur-game-state)]
    game-state-after-player-removal))

(defn add-requests [player-state new-requests]
  (assoc-in player-state
            [:from-requests]
            (into (:from-requests player-state) new-requests)))

(defn increment-wait-time [from-request]
  (update-in from-request [:waited] inc))

(defn increment-wait-times [player-state]
  (update-in player-state [:from-requests] #(map increment-wait-time %)))

(defn remove-requests-that-have-waited-too-long-and-update-unhappy-tally [player-state]
  (let [request-groups (group-by #(< (:waited %) *max-wait-time*) (:from-requests player-state))
        happy-group (empty-if-nil (get request-groups true))
        unhappy-group (empty-if-nil (get request-groups false))]
    (-> player-state
      (assoc :from-requests happy-group)
      (update-in [:tally :unhappy] + (count unhappy-group)))))

(defn increment-tick [player-state]
  (update-in player-state [:tick] inc))

(defn advance-player-state [player-state new-requests]
  (-> player-state
    (increment-tick)
    (increment-wait-times)
    (remove-requests-that-have-waited-too-long-and-update-unhappy-tally)
    (update-elevator-state)
    (add-requests new-requests)))

(defn advance-game-state [state]
  (if (and (running?) (not-empty state))
    (let [first-player-state (first state)
          current-tick (:tick first-player-state);TODO should tick be in game state instead?
          new-from-requests (generate-requests *number-of-floors* current-tick)]
      (map #(advance-player-state % new-from-requests) state))
    state))