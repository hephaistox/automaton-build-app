(ns automaton-build-app.tasks.lint
  (:require [automaton-build-app.app :as build-app]
            [automaton-build-app.code-helpers.lint :as build-lint]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.cicd.cfg-mgt]
            [automaton-build-app.os.exit-codes :as build-exit-codes]))

(defn lint
  "Linter"
  [_cli-opts app _bb-edn-args]
  (when-not (build-lint/lint false (build-app/src-dirs app))
    (build-log/fatal "Linters have failed")
    (System/exit build-exit-codes/catch-all)))
