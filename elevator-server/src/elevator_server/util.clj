(ns elevator-server.util)

(defn empty-if-nil [val]
  (if (nil? val)
    []
    val))