(ns elevator-server.rest-client
  (:use [clojure.tools.logging])
  (:require [elevator-server.core :refer [transform-player-state-to-public update-game-state]]
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
                                :socket-timeout 3000
                                :conn-timeout 3000
                                :accept :json})
          (catch Exception e
            (do
              (error (str (.getMessage e) ": Failed to get answer from: " player-key))
              nil)))
        (parse-body))))

(defn poll-for-action [player-key public-player-state]
  (let [result-body (do-post player-key public-player-state)
        current-floor (get-in public-player-state [:elevator :current-floor])]
    (if (nil? result-body)
          current-floor
          (:go-to result-body))))

(defn poll-client [player-key player-state]
    (poll-for-action player-key (transform-player-state-to-public player-key player-state)))

(defn get-elevator-target-floor-from-player [player-key player-state]
  (let [new-wanted-target-floor (poll-client player-key player-state)
        new-target-floor (keep-floor-target-inside-boundaries new-wanted-target-floor)]
    (assoc-in player-state [:elevator :going-to] new-target-floor)))

(defn request-and-update-target-floor [[player-key player-state]]
  (let [updated-state (get-elevator-target-floor-from-player player-key player-state)]
    (do
      (log/debugf "Polling player %s" player-key)
      (update-game-state #(assoc % player-key updated-state)))))

(defn update-all-elevator-target-floors [game-state]
  (let [player-keys-and-states (seq game-state)
        threads (map request-and-update-target-floor player-keys-and-states)
        tasks (map #(.submit poller-thread-pool %) threads)]
    (dorun tasks)))
