(ns manual.controller
  (require [cheshire.core :as json]))

(import '(javax.swing JFrame JPanel JButton)
        '(java.awt Dimension GridLayout)
        'java.awt.event.ActionListener)

(def target (atom 1))

(defn get-target []
  @target)

(defn set-target [new-target]
  (do
    (println (str "Setting target to " new-target))
    (reset! target new-target)))

(def number-of-floors (atom 5))

(defn get-number-of-floors []
  @number-of-floors)

(defn set-number-of-floors [new-number-of-floors]
  (do
    (println (str "Setting number of floors to " new-number-of-floors))
    (reset! number-of-floors new-number-of-floors)))

(defn create-change-target-action [num]
  (proxy [ActionListener] []
    (actionPerformed [event] (set-target num))))

(defn create-button [num]
  (doto (JButton. (str num))
        (.setPreferredSize (Dimension. 100 50))
        (.addActionListener (create-change-target-action num))))

(defn add-buttons [panel number-of-buttons]
  (dotimes [n number-of-buttons]
    (.add panel (create-button (- number-of-buttons n)))))

(defn create-and-set-panel [frame floors]
  (.setContentPane frame
    (doto (JPanel.)
          (.setLayout (GridLayout. 0 1))
          (add-buttons floors))))

(def frame (doto (JFrame. "Elevator control panel")
                 (.setMinimumSize (Dimension. 100 300))
                 (create-and-set-panel (get-number-of-floors))
                 (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)))

(defn format-response [floor-to-go]
  (json/generate-string {:go-to floor-to-go}))

(defn open-window []
  (.setVisible frame true))

(defn update-button-amount [floors]
  (do
    (set-number-of-floors floors)
    (doto
      frame
      (create-and-set-panel floors)
      (.setMinimumSize (Dimension. 100 (* 50 floors)))
      (.pack))))

(defn decide-floor-to-go [state]
  (do
    (let [floors-in-state (:floors state)]
      (when-not (= floors-in-state (get-number-of-floors))
        (update-button-amount floors-in-state)))
    (format-response (get-target))))