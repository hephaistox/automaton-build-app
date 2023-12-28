(ns automaton-build-app.tasks.tasks
  (:require [automaton-build-app.tasks.registry :as build-task-registry]
            [automaton-build-app.cicd.cfg-mgt]
            [automaton-build-app.os.exit-codes :as build-exit-codes]))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn exec
  "List all tasks"
  [_task-map
   {:keys [task-registry]
    :as _app-data}]
  (if (nil? (build-task-registry/print-tasks task-registry)) build-exit-codes/ok build-exit-codes/catch-all))
