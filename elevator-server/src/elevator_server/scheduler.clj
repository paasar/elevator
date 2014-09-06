(ns elevator-server.scheduler
  (:require [elevator-server.core :refer [get-game-state update-game-state advance-game-state log-game-state]]
            [elevator-server.rest-client :refer [update-all-elevator-target-floors]]
            [clojurewerkz.quartzite.scheduler :as qs]
            [clojurewerkz.quartzite.triggers :as t]
            [clojurewerkz.quartzite.jobs :as j]
            [clojurewerkz.quartzite.jobs :refer [defjob]]
            [clojurewerkz.quartzite.schedule.calendar-interval :refer [schedule with-interval-in-seconds]]))

(defjob game-advancing-job [ctx]
  (update-game-state #(advance-game-state %)))

(defjob client-poller-job [ctx]
  (update-all-elevator-target-floors (get-game-state)))

(defjob state-print-job [ctx]
  (log-game-state))

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
    (start-job game-advancing-job "jobs.advance-game-state" "game advance trigger" 1)
    (start-job client-poller-job "jobs.client-poller" "client poller trigger" 1)
    (start-job state-print-job "jobs.state-print" "state print trigger" 1)))