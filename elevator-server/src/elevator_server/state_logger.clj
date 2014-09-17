(ns elevator-server.state-logger
  (:require [clojure.tools.logging :as log]
            [cheshire.core :as json]))

; Game state logging is in own namespace for easier logging configuration.

(defn log-game-state-to-file [game-state]
  (log/infof (json/generate-string game-state)))