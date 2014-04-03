(ns elevator-server.elevator-state)

(defn get-next-elevator-state [current-floor target-floor has-riders has-newcomers]
  (cond
    (= target-floor current-floor)
      (cond
        has-riders :disembarking
        has-newcomers :embarking
        :else :waiting)
    (> target-floor current-floor) :ascending
    :else :descending))

(defn set-elevator-target-floor [player-state target-floor]
  (assoc-in player-state [:elevator :going-to] target-floor))

(defn set-elevator-current-floor [player-state current-floor]
  (assoc-in player-state [:elevator :current-floor] current-floor))

(defn set-elevator-state [player-state target-floor]
  (let [elevator (:elevator player-state)
        current-floor (:current-floor elevator)
        has-riders (not (empty? (:to-requests elevator)))
        has-newcomers (not (empty? (:from-requests player-state)))
        old-state (:state elevator)
        new-state (get-next-elevator-state current-floor target-floor has-riders has-newcomers)]
    (assoc-in player-state [:elevator :state] new-state)))

(defn set-new-target-floor [player-state target-floor]
  (-> player-state
    (set-elevator-target-floor target-floor)
    (set-elevator-state target-floor)))

(defn get-floor-in-next-step [current-floor state]
  (cond
    (= :ascending state) (inc current-floor)
    (= :descending state) (dec current-floor)
    :else current-floor))

(defn update-elevator-state [player-state]
  (let [elevator (:elevator player-state)
        new-current-floor (get-floor-in-next-step
                            (:current-floor elevator)
                            (:state elevator))]
    (-> player-state
      (set-elevator-current-floor new-current-floor)
      (set-elevator-state (:going-to elevator)))))