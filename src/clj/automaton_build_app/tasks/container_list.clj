(ns automaton-build-app.tasks.container-list
  (:require [automaton-build-app.containers.local-engine :as build-local-engine]
            [automaton-build-app.os.terminal-msg :as build-terminal-msg]
            [automaton-build-app.os.exit-codes :as build-exit-codes]))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn exec
  "List all available containers"
  [_task-map _app]
  (let [[exit-code res] (-> (build-local-engine/container-image-list)
                            first)]
    (when (zero? exit-code) (build-terminal-msg/println-msg res) build-exit-codes/ok)
    exit-code))
