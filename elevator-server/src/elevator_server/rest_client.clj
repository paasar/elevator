(ns elevator-server.rest-client
  (:use [clojure.tools.logging])
  (:require [elevator-server.core :refer [transform-player-state-to-public update-goto]]
            [elevator-server.util :refer [keep-floor-target-inside-boundaries not-nil?]]
            [elevator-server.constants :refer [*socket-timeout* *connection-timeout*]]
            [clj-http.client :as client]
            [cheshire.core :as json]
            [clojure.tools.logging :as log]))
(import '[java.util.concurrent Executors ExecutorService Callable])

(def ^ExecutorService poller-thread-pool (Executors/newCachedThreadPool))

(defn parse-body [response]
  (let [response-body (:body response)]
    (try
      (json/parse-string response-body true)
      (catch Exception e
        (do
          (log/errorf "Response JSON parsing failed: %s" (.getMessage e))
          nil)))))

(defn do-post [player-key public-player-state]
  (let [address (str "http://" (:ip player-key) ":" (:port player-key))]
    (-> (try
          ;TODO timeout error handling?
          (client/post address {:body (json/generate-string public-player-state)
                                :content-type :json
                                :socket-timeout *socket-timeout*
                                :conn-timeout *connection-timeout*
                                :accept :json})
          (catch Exception e
            (do
              (log/errorf "%s: Failed to get answer from: %s" (.getMessage e) player-key)
              nil)))
        (parse-body))))

(defn poll-for-action [player-key public-player-state]
  (let [result-body (do-post player-key public-player-state)
        new-go-to (:go-to result-body)
        current-floor (get-in public-player-state [:elevator :currentFloor])]
    (if (and (not-nil? result-body) (not-nil? new-go-to) (integer? new-go-to))
      new-go-to
      (do
        (log/warnf "Player %s returned malformed response. Response body was: %s" player-key result-body)
        current-floor))))

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
