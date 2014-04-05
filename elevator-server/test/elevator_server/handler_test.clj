(ns elevator-server.handler-test
  (:use clojure.test
        ring.mock.request
        elevator-server.handler))

(deftest test-app
;  (testing "internal state"
;    (let [response (app (request :get "/"))]
;      (is (= (:status response) 200))
;      (is (= (:body response) "Hello World"))))

;  (testing "public state"
;    TODO)

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= (:status response) 404)))))