(ns elevator.logic
  (require [cheshire.core :as json]
           [clojure.tools.logging :as log]))

; The actual logic that decides where to go next.
;
; state example:
;   {"elevator":
;        {"toRequests": [1, 3, 5, 5],
;         "currentFloor": 1,
;         "goingTo": 1,
;         "state": "WAITING",
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

(defn format-response [floor-to-go]
  (json/generate-string {:go-to floor-to-go}))

(defn get-next-in-rotation [current-floor current-target top-floor]
  (if (= current-floor current-target)
    (if (= current-floor 1)
      top-floor
      (dec current-floor))
    current-target))

(defn to-top-or-one-down [state]
  (let [elevator (:elevator state )
        current-floor (:currentFloor elevator)
        current-target (:goingTo elevator)
        top-floor (:floors state)]
     (get-next-in-rotation current-floor current-target top-floor)))

(defn decide-floor-to-go [state]
  (do
    (log/infof "Server is asking where to go.")
    (log/debugf "PlayerState:\n%s" (json/generate-string state {:pretty true}))
    (let [go-to (to-top-or-one-down state)]
      (do
        (log/infof "I want to go to %s" go-to)
        (format-response go-to)))))