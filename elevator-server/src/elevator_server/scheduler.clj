(ns elevator-server.scheduler
  (use elevator-server.data)
  (:require [clojurewerkz.quartzite.scheduler :as qs]
            [clojurewerkz.quartzite.triggers :as t]
            [clojurewerkz.quartzite.jobs :as j]
            [clojurewerkz.quartzite.jobs :refer [defjob]]
            [clojurewerkz.quartzite.schedule.calendar-interval :refer [schedule with-interval-in-seconds]]))


(defjob update-job [ctx]
  (set-state (add-first-floor-request (get-state))))

(defn start-update-job []
  (qs/initialize)
  (qs/start)
  (let [job (j/build
              (j/of-type update-job)
              (j/with-identity (j/key "jobs.update")))
        trigger (t/build
                  (t/with-identity (t/key "every second trigger"))
                  (t/start-now)
                  (t/with-schedule (schedule
                                     (with-interval-in-seconds 1))))]
    (qs/schedule job trigger)))