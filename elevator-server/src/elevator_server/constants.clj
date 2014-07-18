(ns elevator-server.constants)

;TODO dynamic var
(def ^:dynamic *number-of-floors* 5)

(def ^:dynamic *capacity* *number-of-floors*)

(def ^:dynamic *impatience-start* 5)

(def ^:dynamic *max-wait-time* (* 2 *impatience-start*))