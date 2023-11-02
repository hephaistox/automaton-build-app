(ns automaton-build-app.tasks.format-files
  (:require [automaton-build-app.app :as build-app]
            [automaton-build-app.code-helpers.formatter :as build-code-formatter]))

(defn format-files
  "Format all code files"
  [_parsed-cli-opts]
  (let [app-dir ""
        app-data (@build-app/build-app-data_ app-dir)
        src-paths (build-app/src-dirs app-data)]
    (apply build-code-formatter/format-all-app src-paths)))
