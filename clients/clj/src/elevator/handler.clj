(ns elevator.handler
  (:use compojure.core
        elevator.logic)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [cheshire.core :as json]))

(def default-message "I'm a little elevator. Please POST state here to get where I want to go.")

(defroutes app-routes
  (GET "/" [] default-message)
  (POST "/" {body :body} (decide-floor-to-go (json/parse-string (slurp body) true)))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
