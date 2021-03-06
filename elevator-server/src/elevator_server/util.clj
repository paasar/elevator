(ns elevator-server.util
  (:require [elevator-server.constants :refer [*number-of-floors*]]
            [clojure.string :refer [blank?]]))

(defn empty-if-nil [val]
  (if (nil? val)
    []
    val))

(defn keep-floor-inside-boundaries [target]
  (cond
    (< target 1)
      1
    (> target *number-of-floors*)
      *number-of-floors*
    :else target))

(defn empty-str? [s]
  (or (blank? s) (= s "null")))

(def not-nil? (complement nil?))

(def not-empty? (complement empty?))

(defn in? [what coll]
  (not-nil? (some #(= % what) coll)))