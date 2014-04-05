(ns elevator-server.handler
  (:use compojure.core
        elevator-server.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [elevator-server.scheduler :as scheduler]
            [cheshire.core :refer [generate-string]]))

(defroutes app-routes
  (GET "/state/internal" [] (generate-string (get-game-state)))
  (GET "/state" [] (generate-string (transform-game-state-to-public (get-game-state))))
  ;(GET "/" [] "TODO player creation form")
  (POST "/player" [post-data] "TODO create new player")
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (do
    (set-game-state (vector (create-new-player-state)));TODO in final product state data is created when player is added
    (scheduler/start-update-job)
    (handler/site app-routes)))
