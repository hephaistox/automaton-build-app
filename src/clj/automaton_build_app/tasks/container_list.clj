(ns automaton-build-app.tasks.container-list
  (:require [automaton-build-app.containers.local-engine :as build-local-engine]
            [automaton-build-app.log :as build-log]))

(defn container-list
  "List all available containers"
  [{:keys [min-level details]
    :as _parsed-cli-opts}]
  (build-log/set-min-level! min-level)
  (build-log/set-details? details)
  (println (build-local-engine/container-image-list)))
