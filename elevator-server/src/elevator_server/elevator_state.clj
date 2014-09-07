(ns elevator-server.elevator-state
  (:require [elevator-server.util :refer [empty-if-nil keep-floor-target-inside-boundaries]]))

(defn get-state-as-keyword [elevator]
  (keyword (:state elevator)))

(defn set-elevator-state [player-state new-state]
  (assoc-in player-state [:elevator :state] (name new-state)))

(defn waiting-ascending-or-descending [target-floor current-floor]
  (if (= target-floor current-floor)
    :waiting
    (if (> target-floor current-floor)
      :ascending
      :descending)))

(defn get-next-elevator-state [current-floor target-floor current-state has-riders has-newcomers]
  (cond
    (= :disembarking current-state)
      (if has-newcomers
        :embarking
        (waiting-ascending-or-descending target-floor current-floor))
    (= :embarking current-state)
      (waiting-ascending-or-descending target-floor current-floor)
    (or (= :ascending current-state) (= :descending current-state))
      (cond
        (< target-floor current-floor)
          :descending
        (> target-floor current-floor)
          :ascending
        :else
          (cond
            has-riders :disembarking
            has-newcomers :embarking
            :else :waiting))
    :else (waiting-ascending-or-descending target-floor current-floor)))

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
        elevator (:elevator player-state)
        space-available (- (:capacity elevator) (count (:to-requests elevator)))
        embarkers (empty-if-nil (get request-groups true))
        embarkers-that-fit (take space-available embarkers)
        embarkers-that-do-not-fit (drop space-available embarkers)
        new-rider-targets (map :to embarkers-that-fit)
        in-other-floors (empty-if-nil (get request-groups false))
        new-requests (into in-other-floors embarkers-that-do-not-fit)]
    (-> player-state
      (update-in [:elevator :to-requests] into new-rider-targets)
      (assoc :from-requests new-requests))))

(defn disembark-embark [player-state current-state current-floor]
  (cond
    (= current-state :disembarking)
      (disembark player-state current-floor)
    (or (= current-state :embarking) (= current-state :waiting))
      (embark player-state current-floor)
    :else player-state))

(defn resolve-new-elevator-state [player-state target-floor]
  (let [elevator (:elevator player-state)
        current-floor (:current-floor elevator)
        has-riders (not (empty? (:to-requests elevator)))
        has-newcomers (not (empty? (:from-requests player-state)))
        old-state (get-state-as-keyword elevator)
        new-state (get-next-elevator-state current-floor target-floor old-state has-riders has-newcomers)]
    (-> player-state
      (set-elevator-state new-state)
      (disembark-embark old-state current-floor))))

(defn set-new-target-floor [player-state target-floor]
  (-> player-state
    (set-elevator-target-floor target-floor)
    (resolve-new-elevator-state target-floor)))

(defn get-floor-in-next-step [current-floor state]
  (cond
    (= :ascending state) (keep-floor-target-inside-boundaries (inc current-floor))
    (= :descending state) (keep-floor-target-inside-boundaries (dec current-floor))
    :else current-floor))

(defn update-elevator-state [player-state]
  (let [elevator (:elevator player-state)
        new-current-floor (get-floor-in-next-step
                            (:current-floor elevator)
                            (get-state-as-keyword elevator))]
    (-> player-state
      (set-elevator-current-floor new-current-floor)
      (resolve-new-elevator-state (:going-to elevator)))))