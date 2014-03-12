(ns elevator-server.core
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [cheshire.core :as json]))

(defroutes app-routes
  (GET "/" [] (str (json/parse-string (slurp "resources/state.json") true)))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
