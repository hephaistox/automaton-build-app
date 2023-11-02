(ns automaton-build-app.tasks.format-files
  (:require [automaton-build-app.app :as build-app]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.code-helpers.formatter :as build-code-formatter]))

(defn format-files
  "Format all code files"
  [{:keys [min-level details]
    :as _parsed-cli-opts}]
  (build-log/set-min-level! min-level)
  (build-log/set-details? details)
  (let [app-dir ""
        app-data (@build-app/build-app-data_ app-dir)
        src-paths (build-app/src-dirs app-data)]
    (apply build-code-formatter/format-all-app src-paths)))
