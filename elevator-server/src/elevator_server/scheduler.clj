(ns elevator-server.scheduler
  (:require [elevator-server.core :refer [get-game-state update-game-state advance-game-state]]
            [elevator-server.rest-client :refer [update-all-elevator-target-floors]]
            [elevator-server.state-logger :refer [log-game-state-to-file]]
            [elevator-server.constants :refer [*step-interval-secs*]]
            [clojurewerkz.quartzite.scheduler :as qs]
            [clojurewerkz.quartzite.triggers :as t]
            [clojurewerkz.quartzite.jobs :as j]
            [clojurewerkz.quartzite.jobs :refer [defjob]]
            [clojurewerkz.quartzite.schedule.calendar-interval :refer [schedule with-interval-in-seconds]]
            [clojure.tools.logging :as log]))

(defjob game-advancing-job [ctx]
  (do
    (log/debugf "Calling players")
    (update-all-elevator-target-floors (get-game-state))
    (log/debugf "Called players -> Advancing game state")
    (update-game-state #(advance-game-state %))
    (log/debugf "Game state advanced")
    (log-game-state-to-file (get-game-state))))

(defn start-job [job-function job-identity trigger-identity interval]
  (qs/initialize)
  (qs/start)
  (let [job (j/build
              (j/of-type job-function)
              (j/with-identity (j/key job-identity)))
        trigger (t/build
                  (t/with-identity (t/key trigger-identity))
                  (t/start-now)
                  (t/with-schedule (schedule
                                     (with-interval-in-seconds interval))))]
    (qs/schedule job trigger)))

(defn start-jobs []
  (do
    (start-job game-advancing-job "jobs.advance-game-state" "game advance trigger" *step-interval-secs*)))