(ns elevator.logic
  (require [cheshire.core :as json]
           [clojure.tools.logging :as log]))

(def target (atom 1))

(defn format-response [floor-to-go]
  (json/generate-string {:go-to floor-to-go}))

(defn get-next-in-rotation [current-floor top-floor]
  (if (= current-floor top-floor)
    1
    (inc current-floor)))

(defn one-up-and-from-top-to-bottom [state]
  (let [_ (log/infof "Server is asking where to go.")
        current-floor (get-in state [:elevator :current-floor])
        top-floor (get state :floors)
        next-in-rotation (get-next-in-rotation current-floor top-floor)
        current-target @target]
    (if (= current-floor current-target)
      (do
        (log/infof "I want to go to %s." next-in-rotation)
        (reset! target next-in-rotation)
        next-in-rotation)
      current-target)))

(defn decide-floor-to-go [state]
  (do
    ;(println (str "-> " state))
    (format-response
      (one-up-and-from-top-to-bottom state))))