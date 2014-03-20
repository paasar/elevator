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
    (set-internal-state (vector (create-new-state-data)));TODO in final product state data is created when player is added
    (scheduler/start-update-job)
    (handler/site app-routes)))
