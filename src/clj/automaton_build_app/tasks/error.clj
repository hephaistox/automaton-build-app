(ns automaton-build-app.tasks.error
  (:require [automaton-build-app.log :as build-log]
            [automaton-build-app.os.exit-codes :as build-exit-codes]))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn exec [_task-map _app-data] (build-log/fatal "This error is intentional.") build-exit-codes/intentional)
