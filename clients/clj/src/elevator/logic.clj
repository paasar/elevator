(ns elevator.logic
  (require [cheshire.core :as json]))

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
        (reset! target next-in-rotation)
        next-in-rotation)
      current-target)))

(defn decide-floor-to-go [state]
  (do
    ;(println (str "-> " state))
    (format-response
      (one-up-and-from-top-to-bottom state))))