(ns elevator-server.util)

(defn empty-if-nil [val]
  (if (nil? val)
    []
    val))

(defn keep-floor-target-inside-boundaries [target min max]
  (cond
    (< target min)
      min
    (> target max)
      max
    :else target))