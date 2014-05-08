(ns elevator-server.handler
  (:use compojure.core
        ring.util.response)
  (:require [elevator-server.core :as c :refer [get-game-state
                                                set-game-state
                                                stop-game
                                                run-game
                                                create-new-player-state]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [elevator-server.view :as v :refer [transform-game-state-to-view-data]]
            [elevator-server.scheduler :as scheduler]
            [cheshire.core :as json :refer [generate-string]]))

(defn file [path]
  (resource-response path {:root "public"}))

(defroutes app-routes
  (GET "/" [] (file "player-creation.html"))
  (POST "/player" [post-data] "TODO create new player")

  (GET "/game" [] (file "game.html"))

  (GET "/state" [] (generate-string (v/transform-game-state-to-view-data (c/get-game-state))))
  (GET "/state/internal" [] (json/generate-string (c/get-game-state)))

  ;TODO (GET "/admin" [] (file "admin.html")
  (GET "/start" [] (do
                     (c/run-game)
                     "Started"))
  (GET "/stop" [] (do
                    (c/stop-game)
                    "Stopped"))

  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (do
    (c/set-game-state (vector (c/create-new-player-state)));TODO in final product state data is created when player is added
    (scheduler/start-update-job)
    (handler/site app-routes)))
