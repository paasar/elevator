(ns elevator.logic
  (require [cheshire.core :as json]
           [clojure.tools.logging :as log]))
(import 'java.lang.Math)

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

(defn get-most-urgent-or-middle [sorted-by-most-requests top-floor]
  (if-let [floor-with-most-request (first sorted-by-most-requests)]
    (key floor-with-most-request)
    (int (Math/ceil (/ top-floor 2)))))

(defn sort-descending [grouper coll]
  (reverse (sort-by #(count (val %)) (group-by grouper coll))))

(defn hal-9000 [state]
  (let [elevator (:elevator state)
        current-floor (:currentFloor elevator)
        current-target (:goingTo elevator)
        top-floor (:floors state)
        to-reqs (:toRequests elevator)
        to-reqs-sorted (sort-descending identity to-reqs)
        from-reqs-sorted (sort-descending :floor (:fromRequests state))]
    (do
;      (log/infof (str to-reqs-sorted))
;      (log/infof (str from-reqs-sorted))
      (if (empty? to-reqs)
        (get-most-urgent-or-middle from-reqs-sorted top-floor)
        ;TODO stop in floors between here and there
        (key (first to-reqs-sorted))))))

(defn decide-floor-to-go [state]
  (do
    (log/infof "Server is asking where to go.")
    (log/debugf "PlayerState:\n%s" (json/generate-string state {:pretty true}))
    (let [go-to (hal-9000 state)]
      (do
        (log/infof "I want to go to %s" go-to)
        (format-response go-to)))))