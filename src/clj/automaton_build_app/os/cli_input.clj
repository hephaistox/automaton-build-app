(ns automaton-build-app.os.cli-input
  (:require [automaton-build-app.log :as build-log]))

(defn yes-question
  "Asks user a `msg` and expects yes input. Returns true or false based on the response."
  ([msg force?] (if force? true (do (build-log/warn msg) (flush) (contains? #{'y 'Y 'yes 'Yes 'YES} (read)))))
  ([msg] (yes-question msg false)))
