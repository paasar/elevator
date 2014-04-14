(ns elevator-server.handler
  (:use compojure.core
        elevator-server.core
        ring.util.response)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [elevator-server.view :refer [transform-game-state-to-view-data]]
            [elevator-server.scheduler :as scheduler]
            [cheshire.core :refer [generate-string]]))

(defn file [path]
  (resource-response path {:root "public"}))

(defroutes app-routes
  (GET "/" [] (file "player-creation.html"))
  (POST "/player" [post-data] "TODO create new player")

  (GET "/game" [] (file "game.html"))

  (GET "/state" [] (generate-string (transform-game-state-to-view-data (get-game-state))))
  (GET "/state/internal" [] (generate-string (get-game-state)))

  ;TODO (GET "/admin" [] (file "admin.html")
  (GET "/start" [] "TODO start command")
  (GET "/stop" [] "TODO stop command")

  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (do
    (set-game-state (vector (create-new-player-state)));TODO in final product state data is created when player is added
    (scheduler/start-update-job)
    (handler/site app-routes)))
