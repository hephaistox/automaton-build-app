(ns automaton-build-app.tasks.gha
  (:require [automaton-build-app.log :as build-log]
            [automaton-build-app.tasks.ltest :as build-task-ltest]
            [automaton-build-app.os.exit-codes :as build-exit-codes]))

(defn gha
  "Run the test on github actions"
  [task-arg app-dir app-data bb-edn-args]
  (let [forced? (get-in task-arg [:options :force])]
    (if (or (System/getenv "CI") forced?)
      (build-task-ltest/ltest task-arg app-dir app-data bb-edn-args)
      (do (build-log/fatal "This task is meant for CI, use `bb ltest` instead (or -f to force it to test it locally)")
          (System/exit build-exit-codes/catch-all)))))
