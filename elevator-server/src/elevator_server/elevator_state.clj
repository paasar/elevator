(ns elevator-server.elevator-state
  (:require [elevator-server.data :refer [empty-if-nil]]))

(defn get-next-elevator-state [current-floor target-floor current-state has-riders has-newcomers]
  (cond
    (= target-floor current-floor)
      (cond
        (= :disembarking current-state)
         (if has-newcomers
           :embarking
           :waiting)
        (= (or (:ascending current-state) (= :descending current-state)))
          (cond
            has-riders :disembarking
            has-newcomers :embarking
            :else :waiting)
        :else :waiting)
    (> target-floor current-floor) :ascending
    :else :descending))

(defn set-elevator-target-floor [player-state target-floor]
  (assoc-in player-state [:elevator :going-to] target-floor))

(defn set-elevator-current-floor [player-state current-floor]
  (assoc-in player-state [:elevator :current-floor] current-floor))

(defn disembark [player-state current-floor]
  (let [rider-groups (group-by #(= current-floor %) (get-in player-state [:elevator :to-requests]))
        leavers (empty-if-nil (get rider-groups true))
        stayers (empty-if-nil (get rider-groups false))]
    (-> player-state
      (assoc-in [:elevator :to-requests] stayers)
      (update-in [:tally :happy] + (count leavers)))))

(defn embark [player-state current-floor]
  (let [request-groups (group-by #(= current-floor (:from %)) (get-in player-state [:from-requests]))
        embarkers (get request-groups true)
        in-other-floors (get request-groups false)]
    (-> player-state
      (update-in [:elevator :to-requests] into embarkers)
      (assoc :from-requests in-other-floors))))

(defn disembark-embark [player-state current-state current-floor]
  (cond
    (= current-state :disembarking)
      (disembark player-state current-floor)
    (or (= current-state :embarking) (= current-state :waiting))
      (embark player-state current-floor)
    :else player-state))

(defn set-elevator-state [player-state target-floor]
  (let [elevator (:elevator player-state)
        current-floor (:current-floor elevator)
        has-riders (not (empty? (:to-requests elevator)))
        has-newcomers (not (empty? (:from-requests player-state)))
        old-state (:state elevator)
        new-state (get-next-elevator-state current-floor target-floor old-state has-riders has-newcomers)]
    (-> player-state
      (assoc-in [:elevator :state] new-state)
      (disembark-embark old-state current-floor))))

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