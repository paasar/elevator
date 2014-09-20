(ns elevator-server.core
  (:require [cheshire.core :as json]
            [clojure.core.incubator :refer [dissoc-in]]
            [elevator-server.elevator-state :refer [update-elevator-state]]
            [elevator-server.util :refer [empty-if-nil]]
            [elevator-server.request-generator :refer [generate-requests]]
            [elevator-server.constants :refer [*number-of-floors* *capacity* *impatience-start* *max-wait-time*]]
            [clojure.tools.logging :as log]
            [clojure.string :refer [upper-case]]))

; game-state is a map of player-state maps
; the keys are also a maps like this {:name "team-1" :ip "127.0.0.1" :port "3333"}
; see game-state-example.txt
(def game-state (atom {}))

(defn get-game-state [] @game-state)

(defn set-game-state [new-state] (reset! game-state new-state))

(defn update-game-state [update-fun] (swap! game-state update-fun))

(def player-state-template
  (json/parse-string (slurp "resources/player-state-template.json") true))

(def running (atom true))

(defn stop-game [] (reset! running false))

(defn run-game [] (reset! running true))

(defn running? [] @running)

(defn get-game-state-for-admin []
  (let [player-keys (keys (get-game-state))]
    {"players" player-keys
     "running" (running?)}))

; Gotos are elevators' going-to values that are updated
; by a scheduled job polling clients.
; This map (where key is player-key (same as in game-state))
; is then looked up during each game state advancement step.
(def gotos (atom {}))

(defn get-gotos [] @gotos)

(defn get-goto [player-key]
  (let [goto-in-map (get (get-gotos) player-key)]
    (if (nil? goto-in-map)
      1
      goto-in-map)))

(defn update-gotos [update-fun] (swap! gotos update-fun))

(defn update-goto [player-key new-goto]
  (update-gotos #(assoc % player-key new-goto)))

(defn set-floor-amount [player-state]
  (assoc-in player-state [:floors] *number-of-floors*))

(defn set-elevator-capacity [player-state]
  (assoc-in player-state [:elevator :capacity] *capacity*))

(defn get-direction [request]
  (let [from (:from request)
        to (:to request)]
    (if (< from to)
      "UP"
      "DOWN")))

(defn is-impatient? [waited]
  (>= waited *impatience-start*))

;TODO these transforms could be in own file
(defn transform-from-request-to-public [request]
  {:floor (:from request)
   :direction (get-direction request)
   :impatient (is-impatient? (:waited request))})

(defn transform-key-to [elevator from-key to-key]
  (-> elevator
    (assoc to-key (from-key elevator))
    (dissoc from-key)))

(defn transform-elevator-to-public [elevator]
  "Change the keys from dash format to camel case."
  (-> elevator
    (transform-key-to :to-requests :toRequests)
    (transform-key-to :current-floor :currentFloor)
    (transform-key-to :going-to :goingTo)
    (update-in [:state] upper-case)))

(defn transform-player-state-to-public [player-key player-state]
  "Remove those parts of information that are not supposed to be seen by the players."
  (-> player-state
    (update-in [:elevator] transform-elevator-to-public)
    (assoc :fromRequests (vec (map transform-from-request-to-public (:from-requests player-state))))
    (dissoc :from-requests)
    (assoc :client {:name (:name player-key)})))

(defn transform-game-state-to-public [state]
  (map (fn [[player-key player-state]] (transform-player-state-to-public player-key player-state)) state))

(defn clear-from-requests [player-state]
  (assoc-in player-state [:from-requests] []))

(defn create-new-player-state []
  (-> player-state-template
      set-floor-amount
      set-elevator-capacity
      clear-from-requests))

(defn create-new-player [name ip port]
  "Creates a new player hash map that can be merged into game state.
   Works as a single player game state on its own."
  {
    {:name name
     :ip ip
     :port port}
    (create-new-player-state)
  })

;TODO handle error cases like duplicate name or ip
(defn create-and-add-player [cur-game-state name ip port]
  (let [new-player-state (create-new-player name ip port)
        game-state-with-player-added (merge cur-game-state new-player-state)]
    game-state-with-player-added))

(defn find-player-state-key-by-ip-and-port [cur-game-state ip port]
  (let [cur-keys (keys cur-game-state)]
    (first (filter #(and (= ip (:ip %)) (= port (:port %))) cur-keys))))

(defn delete-player-by-ip-and-port [cur-game-state ip port]
  (let [player-key (find-player-state-key-by-ip-and-port cur-game-state ip port)]
    (if player-key
      (dissoc cur-game-state player-key)
      (do
        (log/infof "Could not find player with given parameters.")
        nil))))

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

(defn update-player-going-to [player-state player-key]
  (assoc-in player-state [:elevator :going-to] (get-goto player-key)))

(defn advance-player-state [player-key player-state new-requests]
  (-> player-state
    (increment-tick)
    (increment-wait-times)
    (remove-requests-that-have-waited-too-long-and-update-unhappy-tally)
    (update-player-going-to player-key)
    (update-elevator-state)
    (add-requests new-requests)))

(defn advance-player-states [cur-state new-requests]
  (into {} (map (fn [[player-key player-state]]
                  [player-key (advance-player-state player-key player-state new-requests)])
                cur-state)))

(defn advance-game-state [state]
  (if (and (running?) (not-empty state))
    (let [first-player-state (first (vals state))
          current-tick (:tick first-player-state);TODO should tick be in game state instead?
          new-from-requests (generate-requests *number-of-floors* current-tick)]
      (advance-player-states state new-from-requests))
    state))

(defn reset-game-state [state]
  (loop [keys (keys state)
         new-state {}]
    (let [current-key (first keys)]
      (if (nil? current-key)
        new-state
        (recur
          (rest keys)
          (create-and-add-player
            new-state
            (:name current-key)
            (:ip current-key)
            (:port current-key)))))))