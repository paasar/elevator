(defproject manual "0.1.0-SNAPSHOT"
  :description "Elevator control panel for manual guidance"
  :url "http://www.houston-inc.com"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.8"]
                 [cheshire "5.3.1"]]
  :plugins [[lein-ring "0.8.11"]]
  :ring {:handler manual.handler/app :port 3333}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
