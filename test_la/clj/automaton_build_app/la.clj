(ns automaton-build-app.la
  (:require [automaton-build-app.cli-test-runner :as build-cli-test-runner]
            [automaton-build-app.tasks.registry.global :as build-task-registry-global]))

(defn run
  "Defines tests to run
  Params:
  * `cli-args` cli arguments to pass to all tasks to tests
  * `app-registry` is a cust-app defined registry for cust specific tasks"
  [app-dir cli-args]
  (let [task-registry (build-task-registry-global/build app-dir)] (build-cli-test-runner/cli-test task-registry cli-args)))
