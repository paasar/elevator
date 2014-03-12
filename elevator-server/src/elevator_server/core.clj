(ns elevator-server.core
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [cheshire.core :as json]))

(def template-content
  (json/parse-string (slurp "resources/state-template.json") true))

(defn create-floor [floor-number]
  (let [floor-template (first (template-content :floors))]
    (assoc-in floor-template [:number] floor-number)))

(defn create-floors [number-of-floors]
  (map #(create-floor %) (range 1 (inc number-of-floors))))

(defn create-new-state-data []
    (assoc-in template-content [:floors] (create-floors 9)))

(defroutes app-routes
  (GET "/" [] (str (create-new-state-data)))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
