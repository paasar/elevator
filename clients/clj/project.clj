(defproject elevator "0.1.0-SNAPSHOT"
  :description "Elevator AI"
  :url "http://houston-inc.com"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.6"]
                 [cheshire "5.3.1"]
                 [org.clojure/tools.logging "0.2.6"]
                 [org.slf4j/slf4j-log4j12 "1.7.5"]]
  :plugins [[lein-ring "0.8.11"]]
  :ring {:handler elevator.handler/app :port 3333}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
