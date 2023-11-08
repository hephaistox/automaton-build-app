(ns automaton-build-app.tasks.lint
  (:require [automaton-build-app.app :as build-app]
            [automaton-build-app.code-helpers.lint :as build-lint]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.cicd.cfg-mgt]
            [automaton-build-app.os.exit-codes :as build-exit-codes]))

(defn lint
  "Linter"
  [_task-arg _app-dir app-data _bb-edn-args]
  (when-not (build-lint/lint false (build-app/src-dirs app-data))
    (build-log/fatal "Tests have failed")
    (System/exit build-exit-codes/catch-all)))
