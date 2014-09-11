(ns manual.handler
  (:require [manual.controller :refer :all]
            [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [cheshire.core :as json]))

(def default-message "I'm a little human driven elevator. Please POST state here to get where I want to go.")

(defn parse-request-json [body]
  (json/parse-string (slurp body) true))

(defroutes app-routes
  (GET "/" [] default-message)
  (POST "/" {body :body} (decide-floor-to-go (parse-request-json body)))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (do
    (open-window)
    (handler/site app-routes)))
