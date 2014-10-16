(ns elevator-server.constants)

; Keep in mind that changing these values will most likely
; break the game view layout since it's filled with
; absolute pixel values.

;TODO utilize dynamic vars in tests
(def ^:dynamic *number-of-floors* 5)

(def ^:dynamic *capacity* (inc *number-of-floors*))

(def ^:dynamic *impatience-start* (+ 3 *number-of-floors*))

(def ^:dynamic *max-wait-time* (* 2 *impatience-start*))

(def ^:dynamic *step-interval-secs* 2)

(def ^:dynamic *socket-timeout* 1500)

(def ^:dynamic *connection-timeout* *socket-timeout*)

(def ^:dynamic *happy-unhappy-ratio* 2)