(ns elevator-server.core-test
  (:require [clojure.test :refer :all]
            [elevator-server.core :refer :all])
  (:use ring.mock.request
        elevator-server.core))

(deftest test-app
;  (testing "main route"
;    (let [response (app (request :get "/"))]
;      (is (= (:status response) 200))
;      (is (= (:body response) "Hello World"))))

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= (:status response) 404)))))