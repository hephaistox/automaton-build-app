(ns automaton-build-app.tasks.container-clear
  (:require [automaton-build-app.containers.local-engine :as build-local-engine]
            [automaton-build-app.log :as build-log]))

(defn container-clear
  [_task-arg _app-dir _app-data _bb-edn-args]
  (build-log/info "Clean the containers")
  (build-local-engine/container-clean))
