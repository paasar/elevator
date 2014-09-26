(ns elevator.logic
  (require [cheshire.core :as json]
           [clojure.tools.logging :as log]))
(import 'java.lang.Math)

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

(def not-empty? (complement empty?))

(defn format-response [floor-to-go]
  (json/generate-string {:go-to floor-to-go}))

(defn sort-descending [grouper coll]
  (reverse (sort-by #(count (val %)) (group-by grouper coll))))

(defn reverse-or-not [current target]
  (if (> current target)
    reverse
    identity))

(defn get-next-floor-to-stop [current target to-requests]
  (do
    (log/infof "c %s t %s tr %s" current target to-requests)
    (let [stops (filter #(and (> % current) (< % target)) to-requests)
          increment (reverse-or-not current target)
          next-stop (first (increment (sort stops)))]
        next-stop)))

(defn go-to-middle [top-floor]
  (int (Math/ceil (/ top-floor 2))))

(defn middle-it-is [top-floor]
  (do
    (log/infof "No to or from reqs. Going to middle.")
    (go-to-middle top-floor)))

(defn get-first-in-from-reqs [from-reqs-sorted]
  (do
    (log/infof "No to-reqs. Taking first from from-reqs-sorted %s" from-reqs-sorted))
  ;TODO get closest floor if more than one with most requests
  (key (first from-reqs-sorted)))

(defn hal-9000 [state]
  (let [elevator (:elevator state)
        current-floor (:currentFloor elevator)
        current-target (:goingTo elevator)
        top-floor (:floors state)
        to-reqs (:toRequests elevator)
        to-reqs-sorted (sort-descending identity to-reqs)
        from-reqs-sorted (sort-descending :floor (:fromRequests state))]
    (do
      (log/infof "I'm currently in %s" current-floor))
    (cond
      (not-empty? to-reqs)
        (do
          (log/infof "I have to-reqs %s" to-reqs)
          (if (some #(= current-floor %) to-reqs)
            (do
              (log/infof "Drop off people to current-floor")
              current-floor)
            (do
              (log/infof "No to-reqs in current-floor. Go to first (%s) in to-reqs-sorted %s " (key (first to-reqs-sorted)) to-reqs-sorted)
              ;TODO get closest floor if more than one with most requests
              (key (first to-reqs-sorted)))))
      (not-empty? from-reqs-sorted) (get-first-in-from-reqs from-reqs-sorted)
      :else (middle-it-is top-floor))))

(defn decide-floor-to-go [state]
  (do
    (log/infof "Server is asking where to go (at %s going to %s (%s))." (:currentFloor (:elevator state)) (:goingTo (:elevator state)) (:state (:elevator state)))
;    (log/debugf "PlayerState:\n%s" (json/generate-string state {:pretty true}))
    (let [go-to (hal-9000 state)]
      (do
        (log/infof "I want to go to %s" go-to)
        (format-response go-to)))))