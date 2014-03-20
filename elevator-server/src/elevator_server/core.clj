(ns elevator-server.core
  (:use compojure.core
        elevator-server.data)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [elevator-server.scheduler :as scheduler]
            [cheshire.core :refer [generate-string]]))

(defroutes app-routes
  (GET "/private/state" [] (generate-string (get-internal-state)))
  (GET "/state" [] (generate-string (transform-state-to-public (get-internal-state))))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (do
    (set-internal-state (create-new-state-data))
    (scheduler/start-update-job)
    (handler/site app-routes)))
