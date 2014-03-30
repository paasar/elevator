(ns elevator.logic)

(defn format-response [floor-to-go]
  (str floor-to-go))

(defn one-up-from-top-to-bottom [state]
  (let [current-floor (get-in state [:elevator :current-floor])
        top-floor (get state :floors)]
    (if (= current-floor top-floor)
      1
      (inc current-floor))))

(defn decide-floor-to-go [state]
  (do
    ;(println (str "-> " state))
    (format-response
      (one-up-from-top-to-bottom state))))