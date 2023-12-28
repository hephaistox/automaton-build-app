(ns automaton-build-app.tasks.la
  (:require [automaton-build-app.la :as build-la]))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn exec
  [_task-map
   {:keys [cli-args task-registry]
    :as _app-data}]
  (build-la/run task-registry cli-args))
