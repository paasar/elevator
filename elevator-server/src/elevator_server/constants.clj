(ns elevator-server.constants)

; Keep in mind that changing these values will most likely
; break the game view layout since it's filled with
; absolute pixel values.

;TODO utilize dynamic vars in tests
(def ^:dynamic *number-of-floors* 5)

(def ^:dynamic *capacity* (inc *number-of-floors*))

(def ^:dynamic *impatience-start* 5)

(def ^:dynamic *max-wait-time* (* 2 *impatience-start*))