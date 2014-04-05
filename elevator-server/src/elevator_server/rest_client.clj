(ns elevator-server.rest-client
  (:require [clj-http.client :as client]
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

(defn check-time []
  (println (client/get "http://date.jsontest.com/")))