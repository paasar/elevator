(ns elevator-server.handler
  (:use compojure.core
        ring.util.response)
  (:require [elevator-server.core :as c :refer [get-game-state
                                                set-game-state
                                                stop-game
                                                run-game
                                                create-new-player-state
                                                create-and-add-player]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [elevator-server.view :as v :refer [transform-game-state-to-view-data]]
            [elevator-server.scheduler :as scheduler]
            [cheshire.core :as json :refer [generate-string]]
            [clojure.tools.logging :as log]))

(defn file [path]
  (resource-response path {:root "public"}))

(defroutes app-routes
  (GET "/" [] (file "player-creation.html"))
  (POST "/player" {ip :remote-addr
                   {team-name :team-name
                    port :port} :params}
    (do
      (log/infof "Create player with: %s %s %s" team-name ip port)
      (let [modified-game-state (c/create-and-add-player (c/get-game-state) team-name ip port)]
        (if modified-game-state
          (do
            (c/set-game-state modified-game-state)
            ;TODO redirect to game view
            "Created")
          ;TODO redirect back to player creation
          "Creation failed"))))

  (DELETE "/player/:ip" [ip]
    (do
      (log/infof "Deleting player with ip %s" ip)
      (let [modified-game-state (c/delete-player (c/get-game-state) ip)]
        (if modified-game-state
          (do
            (log/infof "game state after deletion: %s" modified-game-state)
            (c/set-game-state modified-game-state)
            "Deleted")
          "Deletion failed"))))

  (GET "/game" [] (file "game.html"))

  (GET "/state" [] (generate-string (v/transform-game-state-to-view-data (c/get-game-state))))
  (GET "/state/internal" [] (json/generate-string (c/get-game-state)))

  (GET "/admin" [] (file "admin.html"))
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
;    (c/set-game-state (vector (c/create-new-player-state)));TODO in final product state data is created when player is added
    (scheduler/start-jobs)
    (handler/site app-routes)))
