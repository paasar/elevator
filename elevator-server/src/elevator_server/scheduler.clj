(ns elevator-server.scheduler
  (use elevator-server.core
       elevator-server.rest-client)
  (:require [clojurewerkz.quartzite.scheduler :as qs]
            [clojurewerkz.quartzite.triggers :as t]
            [clojurewerkz.quartzite.jobs :as j]
            [clojurewerkz.quartzite.jobs :refer [defjob]]
            [clojurewerkz.quartzite.schedule.calendar-interval :refer [schedule with-interval-in-seconds]]))
;TODO move these
(defn poll-client [player-state]
  (let [client (:client player-state)
         ip (:ip client)
         port (:port client)]
    (poll-for-action ip port (transform-player-state-to-public player-state))))

(defn update-target [player-state]
  (let [new-target-floor (poll-client player-state)]
    (assoc-in player-state [:elevator :going-to] new-target-floor)))

(defjob update-job [ctx]
  (do
    (set-game-state (map update-target (get-game-state)))
    (set-game-state (advance-game-state (get-game-state)))))

(defn start-update-job []
  (qs/initialize)
  (qs/start)
  (let [job (j/build
              (j/of-type update-job)
              (j/with-identity (j/key "jobs.update")))
        trigger (t/build
                  (t/with-identity (t/key "update trigger"))
                  (t/start-now)
                  (t/with-schedule (schedule
                                     (with-interval-in-seconds 5))))]
    (qs/schedule job trigger)))