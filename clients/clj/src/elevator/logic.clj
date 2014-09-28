(ns elevator.logic
  (require [cheshire.core :as json]
           [clojure.tools.logging :as log]))

; The actual logic that decides where to go next.
;
; Player state example:
;   {"elevator":
;        {"toRequests": [1, 3, 5, 5],
;         "currentFloor": 1,
;         "goingTo": 1,
;         "state": "EMBARKING",
;         "capacity": 6},
;   "floors": 5,
;   "fromRequests":
;     [  {"floor": 1, "impatient": true, "direction": "UP"}
;     ,  {"floor": 2, "impatient": false, "direction": "DOWN"}
;     ],
;   "tally":
;        {"happy": 0,
;         "unhappy": 0},
;   "tick": 3}
;
; Elevator state can be: EMBARKING, DISEMBARKING, ASCENDING or DESCENDING
; EMBARKING can be considered also as idle.

(defn format-response [floor-to-go]
  (json/generate-string {:go-to floor-to-go}))

(defn get-next-in-rotation [elevator-state current-floor current-target top-floor]
  (if (and (= current-floor current-target) (= elevator-state "EMBARKING"))
    (if (= current-floor 1)
      top-floor
      (dec current-floor))
    current-target))

(defn to-top-or-one-down [player-state]
  (let [elevator (:elevator player-state)
        state (:state elevator)
        current-floor (:currentFloor elevator)
        current-target (:goingTo elevator)
        top-floor (:floors player-state)]
     (get-next-in-rotation state current-floor current-target top-floor)))

(defn decide-floor-to-go [player-state]
  (do
    (log/infof "Server is asking where to go.")
    (log/debugf "PlayerState:\n%s" (json/generate-string player-state {:pretty true}))
    (let [go-to (to-top-or-one-down player-state)]
      (do
        (log/infof "I want to go to %s" go-to)
        (format-response go-to)))))