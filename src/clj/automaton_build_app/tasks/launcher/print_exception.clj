(ns automaton-build-app.tasks.launcher.print-exception
  (:require [automaton-build-app.tasks.launcher.print-or-spit :as build-print-or-spit]
            [automaton-build-app.os.terminal-msg :as build-terminal-msg]
            [automaton-build-app.cicd.server :as build-cicd-server]))

(defn print-exception
  "Print an exception in the context of the launcher
  Params:
  * `task-name`
  * `e` exception to display"
  [task-name e]
  (build-terminal-msg/println-msg (format "Error during execution of `%s`, %s`" task-name (pr-str (or (ex-message e) e))))
  (build-print-or-spit/exception (build-cicd-server/is-cicd?) e))
