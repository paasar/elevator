(ns elevator-server.rest-client
  (:use [clojure.tools.logging])
  (:require [elevator-server.core :refer [transform-player-state-to-public update-goto]]
            [elevator-server.util :refer [keep-floor-target-inside-boundaries]]
            [clj-http.client :as client]
            [cheshire.core :as json]
            [clojure.tools.logging :as log]))
(import '[java.util.concurrent Executors ExecutorService Callable])

(def ^ExecutorService poller-thread-pool (Executors/newCachedThreadPool))

(defn parse-body [response]
  (json/parse-string (:body response) true))

(defn do-post [player-key public-player-state]
  (let [address (str "http://" (:ip player-key) ":" (:port player-key))]
    (-> (try
          ;TODO timeout error handling?
          (client/post address {:body (json/generate-string public-player-state)
                                :content-type :json
                                :socket-timeout 1000
                                :conn-timeout 1000
                                :accept :json})
          (catch Exception e
            (do
              (error (str (.getMessage e) ": Failed to get answer from: " player-key))
              nil)))
        (parse-body))))

(defn poll-for-action [player-key public-player-state]
  (let [result-body (do-post player-key public-player-state)
        current-floor (get-in public-player-state [:elevator :currentFloor])]
    (if (or (nil? result-body) (nil? (:go-to result-body)))
      (do
        (log/infof "Player %s returned null as a new target. Result body was: %s" player-key result-body)
        current-floor)
      (:go-to result-body))))

(defn poll-client [player-key player-state]
  (poll-for-action player-key (transform-player-state-to-public player-key player-state)))

(defn request-and-update-target-floor [[player-key player-state]]
  (let [new-wanted-target-floor (poll-client player-key player-state)
        new-target-floor (keep-floor-target-inside-boundaries new-wanted-target-floor)]
    (do
      (log/debugf "Polled player %s for new target %s" player-key new-target-floor)
      (update-goto player-key new-target-floor))))

(defn update-all-elevator-target-floors [game-state]
  (let [player-keys-and-states (seq game-state)
        threads (map request-and-update-target-floor player-keys-and-states)
        tasks (map #(.submit poller-thread-pool %) threads)]
    (dorun tasks)))
