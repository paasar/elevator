(ns elevator-server.rest-client
  (:use [clojure.tools.logging])
  (:require [elevator-server.core :refer [transform-player-state-to-public number-of-floors]]
            [elevator-server.util :refer [keep-floor-target-inside-boundaries]]
            [clj-http.client :as client]
            [cheshire.core :as json]))

(defn parse-body [response]
  (json/parse-string (:body response) true))

(defn call-client [address public-player-state]
  (-> (try
        ;TODO timeout error handling?
        (client/post address {:body (json/generate-string public-player-state)
                              :content-type :json
                              :socket-timeout 3000
                              :conn-timeout 3000
                              :accept :json})
        (catch Exception e
          (do
            (error (str (.getMessage e) ": Failed to get answer from: " address))
            nil)))
    (parse-body)))

(defn poll-for-action [ip port public-player-state]
  (let [address (str "http://" ip ":" port)
        result-body (call-client address public-player-state)
        current-floor (get-in public-player-state [:elevator :current-floor])
;TODO        _ (println current-floor " " result-body)
        ]
    (if (nil? result-body)
          current-floor
          (:go-to result-body))))

(defn poll-client [player-state]
  (let [client (:client player-state)
        ip (:ip client)
        port (:port client)]
    (poll-for-action ip port (transform-player-state-to-public player-state))))

(defn update-elevator-target-floor [player-state]
  (let [new-wanted-target-floor (poll-client player-state)
        new-target-floor (keep-floor-target-inside-boundaries new-wanted-target-floor 1 number-of-floors)]
    (assoc-in player-state [:elevator :going-to] new-target-floor)))