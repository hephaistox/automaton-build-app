(ns automaton-build-app.tasks.is-cicd
  (:require [automaton-build-app.log :as build-log]
            [automaton-build-app.os.exit-codes :as build-exit-codes]))

(defn is-cicd
  "Run the test on github actions"
  [cli-opts _app _bb-edn-args]
  (let [forced? (get-in cli-opts [:options :force])]
    (when-not (or (System/getenv "CI") forced?)
      (build-log/fatal "This task is meant for CI, use `bb ltest` instead (or -f to force it to test it locally)")
      (System/exit build-exit-codes/catch-all))))
