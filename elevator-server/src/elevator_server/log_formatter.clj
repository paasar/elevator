(ns elevator-server.log-formatter
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [cheshire.core :as json :refer [parse-string]]))

;TODO make the actual game print something useful and then remove this abomination

(defn get-player-name [element]
  (let [player-key-str (name (first element))]
    (apply str (drop-last 2 (rest (second (str/split player-key-str #" ")))))))

(defn get-score-str [element]
  (let [player-data (second element)
        tally (:tally player-data)]
    (str (:happy tally) "," (:unhappy tally))))

(defn get-tick [element]
  (:tick (second element)))

(def data
  (doseq [line-str (with-open [rdr (io/reader "game-state.log")]
                 (doall (line-seq rdr)))]
    (let [line (json/parse-string line-str true)
          line-to-write (pr-str (map #(str (get-player-name %) "," (get-tick %) "," (get-score-str %) ",") line))]
      (do
        (println line-to-write)
        (spit "02.csv" line-to-write :append true)))))