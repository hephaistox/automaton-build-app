(ns automaton-build-app.tasks.container-list
  (:require [automaton-build-app.containers.local-engine :as
             build-local-engine]))

(defn container-list
  "List all available containers"
  [_parsed-cli-opts]
  (println (build-local-engine/container-image-list)))
