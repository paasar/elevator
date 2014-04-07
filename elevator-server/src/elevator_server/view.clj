(ns elevator-server.view)

(defn transform-player-state-to-view-data [player-state]
  '())

(defn transform-game-state-to-view-data [state]
  (map transform-player-state-to-view-data state))