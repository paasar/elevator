(ns elevator-server.util
  (:require [elevator-server.constants :refer [number-of-floors]]))

(defn empty-if-nil [val]
  (if (nil? val)
    []
    val))

(defn keep-floor-target-inside-boundaries [target]
  (cond
    (< target 1)
      1
    (> target number-of-floors)
      number-of-floors
    :else target))