(ns elevator-server.core
  (:use compojure.core
        elevator-server.data)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [elevator-server.scheduler :as scheduler]
            [cheshire.core :refer [generate-string]]))

(defroutes app-routes
  (GET "/" [] (generate-string (get-state)))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (do
    (set-state (create-new-state-data))
    (scheduler/start-update-job)
    (handler/site app-routes)))