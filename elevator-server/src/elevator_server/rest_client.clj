(ns elevator-server.rest-client
  (:require [clj-http.client :as client]))

;TODO sane functionality
(defn check-time []
  (println (client/get "http://date.jsontest.com/")))