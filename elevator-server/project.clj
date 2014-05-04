(defproject elevator-server "0.1.0-SNAPSHOT"
  :description "Extreme startup coding competition. Create elevator logic. This is the server project that runs the competition."
  :url "http://houston-inc.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.incubator "0.1.3"]
                 [compojure "1.1.6"]
                 [cheshire "5.3.1"]
                 [clojurewerkz/quartzite "1.2.0"]
                 [clj-http "0.9.1"]]
  :plugins [[lein-ring "0.8.10"]]
  :ring {:handler elevator-server.handler/app}
  :profiles
      {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                            [ring-mock "0.1.5"]]}})