(ns automaton-build-app.tasks.clean
  (:require [automaton-build-app.os.files :as build-files]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.os.exit-codes :as build-exit-codes]))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn exec
  "Clean cache files for compilers to start from scratch"
  [_task-map {:keys [dirs]}]
  (build-log/debug-format "The directories `%s` are cleaned" dirs)
  (build-files/delete-files dirs)
  build-exit-codes/ok)
