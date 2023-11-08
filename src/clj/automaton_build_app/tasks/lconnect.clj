(ns automaton-build-app.tasks.lconnect
  (:require [automaton-build-app.log :as build-log]
            [automaton-build-app.os.commands :as build-cmds]))

(defn lconnect
  "Local connection to the code
  Params:
  * none"
  [_task-arg _app-dir app-data _bb-edn-args]
  (let [aliases (get-in app-data [:lconnect :aliases])]
    (build-log/info-format "Starting repl with aliases `%s`" (apply str aliases))
    (build-cmds/execute-and-trace ["clojure" (apply str "-M" aliases)])))
