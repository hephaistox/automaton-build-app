(ns automaton-build-app.tasks.lbe-test
  (:require [automaton-build-app.log :as build-log]
            [automaton-build-app.os.commands :as build-cmds]
            [automaton-build-app.os.exit-codes :as build-exit-codes]))

(defn lbe-test
  "Local backend tests
  All that tests should be runnable on github action
  `rlwrap` is not on the container image, so `clojure` should be used instead of `clj`"
  [_task-arg _app-dir
   {:keys [ltest]
    :as _app-data} _bb-edn-args]
  (let [aliases (get ltest :aliases)]
    (when-not (build-cmds/execute-and-trace ["clojure" (apply str "-M" aliases)])
      (build-log/fatal "Tests have failed")
      (System/exit build-exit-codes/catch-all))))
