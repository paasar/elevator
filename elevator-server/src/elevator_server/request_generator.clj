(ns elevator-server.request-generator)

(import '(java.lang Math))

(defn- generate-random-request-from [highest-floor current-floor]
  (let [highest-floor-plus-one (inc highest-floor)
        possible-floors (vec (disj (set (range 1 highest-floor-plus-one))
                               current-floor))]
    {:from (int current-floor)
     :to (int (rand-nth possible-floors))
     :waited 0}))

(defn generate-random-request [highest-floor]
  "Development time help function for totally random requests."
  (let [current-floor (inc (rand-int highest-floor))]
    (generate-random-request-from highest-floor current-floor)))

(defn- minus-to-zero-otherwise-ceil [number]
  (if (< number 0)
    0
    (Math/ceil number)))

(defn- generate-tick-based-requests [number-of-floors tick]
  (let [head-count (minus-to-zero-otherwise-ceil (* (Math/sin (* tick 0.2))
                                                    (Math/cos (* tick 0.5))
                                                    (Math/log tick)))
        middle-point (* number-of-floors 0.5)
        floor-a (+ middle-point (* (Math/sin (* tick 0.03))
                                   (Math/cos (* tick 0.04))
                                   middle-point))
        floor-b (+ middle-point (* (Math/cos (* tick 0.05))
                                   (Math/cos (* tick 0.04))
                                   middle-point))
        floor-range (if (< floor-a floor-b)
                      (range (Math/ceil floor-a) (Math/ceil floor-b))
                      (range (Math/ceil floor-b) (Math/ceil floor-a) -1))]
    (if (or (= head-count 0) (empty? floor-range))
      []
      (vec (repeat head-count (generate-random-request-from number-of-floors (rand-nth floor-range)))))))

(defn generate-requests [number-of-floors tick]
  (generate-tick-based-requests number-of-floors tick))
;  (vector (generate-random-request number-of-floors)))