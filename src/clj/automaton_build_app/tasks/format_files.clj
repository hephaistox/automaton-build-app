(ns automaton-build-app.tasks.format-files
  (:require [automaton-build-app.app :as build-app]
            [automaton-build-app.code-helpers.formatter :as build-code-formatter]))

(defn format-files
  "Format all code files"
  [_task-arg _app-dir app-data _bb-edn-args]
  (let [src-paths (build-app/src-dirs app-data)] (apply build-code-formatter/format-all-app src-paths)))
