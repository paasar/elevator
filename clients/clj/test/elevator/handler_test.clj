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

(deftest test-app-post
  (testing "main-post"
    (let [response (app (-> (request :post "/")
                            (body (json/generate-string {:elevator {:current-floor 1}}))))]
      (is (= (:body response) (str 1))))));TODO can we return pure integer?
