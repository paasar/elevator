(ns elevator-server.request-generator)

(defn generate-request [highest-floor]
  (let [current-floor (inc (rand-int highest-floor))
        highest-floor-plus-one (inc highest-floor)
        possible-floors (vec (disj (set (range 1 highest-floor-plus-one))
                               current-floor))]
    {:from current-floor
     :to (rand-nth possible-floors)
     :waited 0}))

(defn generate-requests [number-of-floors tick]
  (vector (generate-request number-of-floors)))