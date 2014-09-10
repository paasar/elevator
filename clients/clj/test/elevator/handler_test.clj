(ns elevator.handler-test
  (:use clojure.test
        ring.mock.request
        elevator.handler)
  (:require [cheshire.core :as json]))

(deftest test-app-get
  (testing "main route"
    (let [response (app (request :get "/"))]
      (is (= (:status response) 200))
      (is (= (:body response) default-message))))
  
  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= (:status response) 404)))))

(defn create-test-status [current-floor going-to]
  {:elevator {:currentFloor current-floor
              :goingTo going-to}
   :floors 3})

(defn create-post-request [current-floor going-to]
  (-> (request :post "/")
      (body (json/generate-string (create-test-status current-floor going-to)))))

(defn go-to-response [floor]
  (json/generate-string {:go-to floor}))

(deftest test-app-post
  (testing "from bottom to top"
    (let [response (app (create-post-request 1 1))]
      (is (= (:body response) (go-to-response 3)))))
  (testing "from two to one"
    (let [response (app (create-post-request 2 1))]
      (is (= (:body response) (go-to-response 1)))))
  (testing "from top to two"
    (let [response (app (create-post-request 3 2))]
      (is (= (:body response) (go-to-response 2))))))