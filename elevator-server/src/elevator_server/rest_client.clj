(ns elevator-server.rest-client
  (:use [clojure.tools.logging])
  (:require [elevator-server.core :refer [transform-player-state-to-public]]
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
        current-floor (get-in public-player-state [:elevator :current-floor])]
    (if (nil? result-body)
          current-floor
          (:go-to result-body))))

(defn poll-client [player-key player-state]
  (let [ip (:ip player-key)
        port (:port player-key)]
    (poll-for-action ip port (transform-player-state-to-public player-key player-state))))

(defn update-elevator-target-floor [player-key player-state]
  (let [new-wanted-target-floor (poll-client player-key player-state)
        new-target-floor (keep-floor-target-inside-boundaries new-wanted-target-floor)]
    (assoc-in player-state [:elevator :going-to] new-target-floor)))

(defn update-all-elevator-target-floors [game-state]
  ;TODO threading
  (let [player-keys-and-states (seq game-state)]
    (into {} (map (fn [[player-key player-state]]
                    [player-key (update-elevator-target-floor player-key player-state)])
                  player-keys-and-states))))