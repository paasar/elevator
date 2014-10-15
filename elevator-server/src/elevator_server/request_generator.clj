(ns elevator-server.request-generator
  (:require [elevator-server.util :refer [keep-floor-inside-boundaries]]))

(import '(java.lang Math))

(defn generate-random-request-from [highest-floor given-from-floor]
  {:pre [(integer? highest-floor)
         (integer? given-from-floor)]}
  (let [highest-floor-plus-one (inc highest-floor)
        from-floor (keep-floor-inside-boundaries given-from-floor)
        possible-floors (vec (disj (set (range 1 highest-floor-plus-one))
                               from-floor))]
    {:from (int from-floor)
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

(defn generate-tick-based-requests [number-of-floors tick]
  (let [;head-count (minus-to-zero-otherwise-ceil (* (Math/sin (* tick 0.2))
        ;                                            (Math/cos (* tick 0.5))
        ;                                            (Math/log (* tick 0.025))))
        head-count (minus-to-zero-otherwise-ceil (- 0.5
                                                   (* 2
                                                      (Math/sqrt (- (Math/sin (* tick 3))
                                                                    (Math/cos (* tick 0.2))))
                                                      (Math/sin (* tick 0.25))
                                                      (Math/sin (* tick 0.0015)))))
        middle-point (* number-of-floors 0.5)
        floor-a (+ middle-point (* (Math/sin (* tick 0.03))
                                   (Math/cos (* tick 0.04))
                                   middle-point))
        floor-b (+ middle-point (* (Math/cos (* tick 0.05))
                                   (Math/cos (* tick 0.04))
                                   middle-point))
        floor-range (if (< floor-a floor-b)
                      (range (Math/ceil floor-a) (inc (Math/ceil floor-b)))
                      (range (inc (Math/ceil floor-b)) (Math/ceil floor-a) -1))]
    (if (or (= head-count 0) (empty? floor-range))
      []
      (vec (take head-count (repeatedly
                              #(generate-random-request-from number-of-floors (int (rand-nth floor-range)))))))))

(defn generate-requests [number-of-floors tick]
  (generate-tick-based-requests number-of-floors tick))
;  (vector (generate-random-request number-of-floors)))