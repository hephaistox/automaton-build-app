(ns automaton-build-app.tasks.lbe-test
  (:require [automaton-build-app.log :as build-log]
            [automaton-build-app.os.commands :as build-cmds]
            [automaton-build-app.os.exit-codes :as build-exit-codes]))

(defn lbe-test
  "Local backend tests
  All that tests should be runnable on github action
  `rlwrap` is not on the container image, so `clojure` should be used instead of `clj`"
  [_cli-opts app _bb-edn-args]
  (if-let [aliases (get-in app [:build-config :ltest :aliases])]
    (when-not (build-cmds/execute-and-trace ["clojure" (apply str "-M" aliases)])
      (build-log/fatal "Tests have failed")
      (System/exit build-exit-codes/catch-all))
    (build-log/warn-format "Backend tests are skipped as the setup is not done (for app `%s`)" (get app :app-name))))
