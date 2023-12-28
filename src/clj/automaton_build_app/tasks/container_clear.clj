(ns automaton-build-app.tasks.container-clear
  (:require [automaton-build-app.containers.local-engine :as build-local-engine]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.os.exit-codes :as build-exit-codes]))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn exec
  [_task-map _app]
  (build-log/info "Clean the containers")
  (if (nil? (build-local-engine/container-clean)) build-exit-codes/ok build-exit-codes/catch-all))
