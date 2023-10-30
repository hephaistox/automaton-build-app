(ns automaton-build-app.tasks.container-clear
  (:require [automaton-build-app.containers.local-engine :as build-local-engine]
            [automaton-build-app.log :as build-log]))

(defn container-clear
  [_parsed-cli-opts]
  (build-log/info "Clean the containers")
  (build-local-engine/container-clean))
