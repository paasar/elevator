(ns elevator-server.rest-client
  (:require [elevator-server.core :refer [transform-player-state-to-public]]
            [clj-http.client :as client]
            [cheshire.core :as json]))

(defn poll-for-action [ip port public-player-state]
  (let [address (str "http://" ip ":" port)
        response (client/post address {:body (json/generate-string public-player-state)
                                     :content-type :json
                                     :socket-timeout 3000
                                     :conn-timeout 3000
                                     :accept :json})
        result-body (json/parse-string (:body response) true)]
    (:go-to result-body)))
;TODO timeout error handling?

(defn poll-client [player-state]
  (let [client (:client player-state)
        ip (:ip client)
        port (:port client)]
    (poll-for-action ip port (transform-player-state-to-public player-state))))

(defn update-elevator-target-floor [player-state]
  (let [new-target-floor (poll-client player-state)]
    (assoc-in player-state [:elevator :going-to] new-target-floor)))