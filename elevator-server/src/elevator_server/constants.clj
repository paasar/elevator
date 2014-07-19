(ns elevator-server.constants)

;TODO utilize dynamic vars in tests
(def ^:dynamic *number-of-floors* 5)

(def ^:dynamic *capacity* *number-of-floors*)

(def ^:dynamic *impatience-start* 5)

(def ^:dynamic *max-wait-time* (* 2 *impatience-start*))