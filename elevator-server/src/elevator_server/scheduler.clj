(ns elevator-server.scheduler
  (:require [elevator-server.core :refer [update-game-state advance-game-state]]
            [elevator-server.rest-client :refer [update-elevator-target-floor]]
            [clojurewerkz.quartzite.scheduler :as qs]
            [clojurewerkz.quartzite.triggers :as t]
            [clojurewerkz.quartzite.jobs :as j]
            [clojurewerkz.quartzite.jobs :refer [defjob]]
            [clojurewerkz.quartzite.schedule.calendar-interval :refer [schedule with-interval-in-seconds]]))

(defjob update-job [ctx]
  (do
    (update-game-state #(map update-elevator-target-floor %))
    (update-game-state #(advance-game-state %))))

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
                                     (with-interval-in-seconds 1))))]
    (qs/schedule job trigger)))