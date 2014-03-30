(ns elevator.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [cheshire.core :as json]))

(def default-message "I'm a little elevator. Please POST state here to get where I want to go.")

(defn decide-floor-to-go [state]
  (do
    ;(println (str "-> " state))
    (let [current-floor (get-in state [:elevator :current-floor])]
      (str current-floor))));TODO can we return pure integer?

(defroutes app-routes
  (GET "/" [] default-message)
  (POST "/" {body :body} (decide-floor-to-go (json/parse-string (slurp body) true)))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
