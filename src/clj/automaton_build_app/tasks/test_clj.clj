(ns automaton-build-app.tasks.test-clj
  (:require [automaton-build-app.log :as build-log]
            [automaton-build-app.tasks.test :as build-task-test]))

(defn gha
  [{:keys [min-level], :as opts}]
  (build-log/set-min-level! min-level)
  (let [forced? (get-in opts [:cli-opts :options :force])]
    (if (or (System/getenv "CI") forced?)
      (build-task-test/ltest opts)
      (build-log/error
        "This task if for CI, use `bb ltest` instead (or -f to force it to test it locally)"))))
