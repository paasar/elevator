(ns elevator-server.elevator-state
  (:require [elevator-server.util :refer [empty-if-nil
                                          keep-floor-inside-boundaries
                                          not-empty?
                                          not-nil?
                                          in?]]))

(defn get-state-as-keyword [elevator]
  (keyword (:state elevator)))

(defn set-elevator-state [player-state new-state]
  (assoc-in player-state [:elevator :state] (name new-state)))

(defn get-up-down-or-same [current-floor target-floor]
  (cond
    (> target-floor current-floor) :up
    (< target-floor current-floor) :down
    :else :same))

(def state-transformations
  {
   ;target is same floor
   [:embarking :same false] :embarking
   [:embarking :same true] :disembarking
   [:disembarking :same false] :embarking
   [:disembarking :same true] :embarking
   [:ascending :same false] :embarking
   [:ascending :same true] :disembarking
   [:descending :same false] :embarking
   [:descending :same true] :disembarking
   ;target is lower
   [:embarking :down false] :descending
   [:embarking :down true] :descending
   [:disembarking :down false] :descending
   [:disembarking :down true] :embarking
   [:descending :down false] :descending
   [:descending :down true] :descending
   [:ascending :down false] :embarking
   [:ascending :down true] :disembarking
   ;target is higher
   [:embarking :up false] :ascending
   [:embarking :up true] :ascending
   [:disembarking :up false] :ascending
   [:disembarking :up true] :embarking
   [:descending :up false] :embarking
   [:descending :up true] :disembarking
   [:ascending :up false] :ascending
   [:ascending :up true] :ascending})

(defn get-next-elevator-state [current-floor target-floor current-state has-riders-for-current-floor]
  {:pre [(integer? current-floor)
         (integer? target-floor)
         (in? current-state [:embarking :disembarking :ascending :descending])
         (in? has-riders-for-current-floor [true false])]
   :post [(not-nil? %)]}
  (let [up-down-or-same (get-up-down-or-same current-floor target-floor)]
    (get state-transformations [current-state up-down-or-same has-riders-for-current-floor])))

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
        embarkers (sort-by :waited > (empty-if-nil (get request-groups true)))
        embarkers-that-fit (take space-available embarkers)
        embarkers-that-do-not-fit (drop space-available embarkers)
        new-rider-targets (map :to embarkers-that-fit)
        in-other-floors (empty-if-nil (get request-groups false))
        updated-from-requests (into in-other-floors embarkers-that-do-not-fit)]
    (-> player-state
      (update-in [:elevator :to-requests] into new-rider-targets)
      (assoc :from-requests updated-from-requests))))

(defn get-floor-in-next-step [current-floor state]
  (cond
    (= :ascending state) (keep-floor-inside-boundaries (inc current-floor))
    (= :descending state) (keep-floor-inside-boundaries (dec current-floor))
    :else current-floor))

(defn move-disembark-or-embark [player-state]
  (let [elevator (:elevator player-state)
        current-floor (:current-floor elevator)
        current-state (get-state-as-keyword elevator)]
    (cond
      (= current-state :disembarking)
        (disembark player-state current-floor)
      (or (= current-state :embarking))
        (embark player-state current-floor)
      :else
        (set-elevator-current-floor player-state (get-floor-in-next-step current-floor current-state)))))

(defn update-elevator-state [player-state]
  (let [elevator (:elevator player-state)
        current-floor (:current-floor elevator)
        target-floor (:going-to elevator)
        old-state (get-state-as-keyword elevator)
        to-requests (:to-requests elevator)
        has-riders-for-current-floor (in? current-floor to-requests)
        new-state (get-next-elevator-state
                    current-floor
                    target-floor
                    old-state
                    has-riders-for-current-floor)]
      (set-elevator-state player-state new-state)))