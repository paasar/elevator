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

(defn create-test-status [current-floor]
  {:elevator {:current-floor current-floor}
   :floors 3})

(defn create-post-request [current-floor]
  (-> (request :post "/")
      (body (json/generate-string (create-test-status current-floor)))))

(deftest test-app-post
  (testing "from one to two"
    (let [response (app (create-post-request 1))]
      (is (= (:body response) (str 2)))));TODO can we return pure integer?
  (testing "from two to top"
    (let [response (app (create-post-request 2))]
      (is (= (:body response) (str 3)))));TODO can we return pure integer?
  (testing "from top to bottom"
    (let [response (app (create-post-request 3))]
      (is (= (:body response) (str 1))))));TODO can we return pure integer?
