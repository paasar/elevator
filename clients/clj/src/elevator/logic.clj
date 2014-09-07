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
;         "state": WAITING,
;         "capacity": 1},
;   "floors": 5,
;   "fromRequests":
;     [  {"floor": 1,
;         "impatient": true,
;         "direction": UP}
;     ,  {"floor": 2,
;         "impatient": false,
;         "direction": DOWN}
;     ],
;   "tally":
;        {"happy": 0,
;         "unhappy": 0},
;   "tick": 3}

(def target (atom 1))

(defn format-response [floor-to-go]
  (json/generate-string {:go-to floor-to-go}))

(defn get-next-in-rotation [current-floor top-floor]
  (if (= current-floor top-floor)
    1
    (inc current-floor)))

(defn one-up-and-from-top-to-bottom [state]
  (let [current-floor (get-in state [:elevator :current-floor])
        top-floor (get state :floors)
        next-in-rotation (get-next-in-rotation current-floor top-floor)
        current-target @target]
    (if (= current-floor current-target)
      (do
        (log/infof "I want to go to %s." next-in-rotation)
        (reset! target next-in-rotation)
        next-in-rotation)
      (do
        (log/infof "I still want to go to %s." current-target)
        current-target))))

(defn decide-floor-to-go [state]
  (do
    (log/infof "Server is asking where to go.")
    (log/debugf "State:\n%s" state)
    (format-response
      (one-up-and-from-top-to-bottom state))))