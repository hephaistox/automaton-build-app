(ns automaton-build-app.os.cli-input
  (:require [automaton-build-app.log :as build-log]))

(defn yes-question
  ([msg force?] (if force? true (do (build-log/warn msg) (flush) (contains? #{'y 'Y 'yes 'Yes 'YES} (read)))))
  ([msg] (yes-question msg false)))
