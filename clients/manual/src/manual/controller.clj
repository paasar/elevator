(ns manual.controller
  (require [cheshire.core :as json]))

(import '(javax.swing JFrame JPanel JButton JOptionPane BoxLayout)
        'java.awt.event.ActionListener
        'java.awt.Dimension)

(def target (atom 1))

(defn get-target []
  @target)

(defn set-target [new-target]
  (do
    (println (str "Setting " new-target))
    (reset! target new-target)))

(defn say-num [num]
  (JOptionPane/showMessageDialog
    nil (str num) "Greeting"
    JOptionPane/INFORMATION_MESSAGE))

(defn create-action [num]
  (proxy [ActionListener] []
    (actionPerformed [event] (set-target num))))

(defn create-button [num]
  (doto (JButton. (str num))
        (.setPreferredSize (Dimension. 100 50))
        (.addActionListener (create-action num))))

(defn add-buttons [panel number-of-buttons]
  (dotimes [n number-of-buttons]
    (.add panel (create-button (- number-of-buttons n)))))

(def panel (doto (JPanel.)
                 #(.setLayout % (BoxLayout. % BoxLayout/Y_AXIS))
                 (add-buttons 5)))

(def frame (doto (JFrame. "Elevator")
                 (.setMinimumSize (Dimension. 100 300))
                 (.setContentPane panel)
                 (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)))
;(.revalidate button)

(defn format-response [floor-to-go]
  (json/generate-string {:go-to floor-to-go}))

(defn open-window []
  (.setVisible frame true))

;TODO update buttons based on floors in state
(defn decide-floor-to-go [state]
  (format-response (get-target)))