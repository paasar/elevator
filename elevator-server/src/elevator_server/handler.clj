(ns elevator-server.handler
  (:use compojure.core
        ring.util.response)
  (:require [elevator-server.core :as c :refer [get-game-state
                                                set-game-state
                                                get-game-state-for-admin
                                                stop-game
                                                run-game
                                                running?
                                                create-new-player-state
                                                create-and-add-player
                                                delete-player-by-ip-and-port]]
            [elevator-server.util :refer [empty-str?]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [elevator-server.view :as v :refer [game-state->view-data]]
            [elevator-server.scheduler :as scheduler]
            [cheshire.core :as json :refer [generate-string]]
            [clojure.tools.logging :as log]))

(defn file [path]
  (resource-response path {:root "public"}))

(defn player-creation-failed []
  (do
    (log/infof "Player creation failed")
    (redirect "/")))

(defroutes app-routes
  (GET "/" [] (file "player-creation.html"))
  (POST "/player" {{team-name :team-name
                    ip :ip-address
                    port :port} :params}
    (if (or (empty-str? team-name) (empty-str? ip) (empty-str? port))
      ;TODO more info to player
      (player-creation-failed)
      (do
        (log/infof "Create player with: %s %s %s" team-name ip port)
        (let [modified-game-state (c/create-and-add-player (c/get-game-state) team-name ip port)]
          (if modified-game-state
            (do
              (c/set-game-state modified-game-state)
              (log/infof "Player created")
              (redirect "/game"))
            (player-creation-failed))))))

  (DELETE "/player/:ip/:port" [ip port]
    (do
      (log/infof "Deleting player with IP %s and port %s" ip port)
      (let [modified-game-state (c/delete-player-by-ip-and-port (c/get-game-state) ip port)]
        (if modified-game-state
          (do
            (c/set-game-state modified-game-state)
            "Deleted")
          "Deletion failed"))))

  (GET "/game" [] (file "game.html"))

  (GET "/state" [] (generate-string (v/game-state->view-data (c/get-game-state))))
  (GET "/state/internal" [] (json/generate-string (c/get-game-state)))
  (GET "/state/admin" [] (json/generate-string (c/get-game-state-for-admin)))

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
    (c/set-game-state {})
    ;TODO developer help: create one player on launch
;    (c/set-game-state (c/create-new-player "A-team" "127.0.0.1" "3333"))
    (scheduler/start-jobs)
    (handler/site app-routes)))
