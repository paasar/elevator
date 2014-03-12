(ns elevator-server.core
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]))

(defroutes app-routes
  (GET "/" [] (str "Hello World" " " (prn-str (map #(* % %) (range 10)))))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
