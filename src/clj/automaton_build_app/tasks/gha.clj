(ns automaton-build-app.tasks.gha
  (:require [automaton-build-app.log :as build-log]
            [automaton-build-app.tasks.ltest :as build-task-ltest]
            [automaton-build-app.os.exit-codes :as build-exit-codes]))

(defn gha
  "Run the test on github actions"
  [{:keys [min-level], :as parsed-cli-opts}]
  (build-log/set-min-level! min-level)
  (let [forced? (get-in parsed-cli-opts [:cli-opts :options :force])]
    (if (or (System/getenv "CI") forced?)
      (build-task-ltest/ltest parsed-cli-opts)
      (do
        (build-log/fatal
          "This task if for CI, use `bb ltest` instead (or -f to force it to test it locally)")
        (System/exit build-exit-codes/catch-all)))))
