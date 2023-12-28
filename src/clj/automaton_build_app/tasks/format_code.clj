(ns automaton-build-app.tasks.format-code
  (:require [automaton-build-app.app-data :as build-app-data]
            [automaton-build-app.code-helpers.formatter :as build-code-formatter]
            [automaton-build-app.os.exit-codes :as build-exit-codes]))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn exec
  "Format all code files"
  [_task-map
   {:keys [include-files]
    :as app-data}]
  (let [repo-path-files (build-app-data/project-paths-files app-data)
        repo-root-files (build-app-data/project-search-files app-data include-files)]
    (if-not (-> (concat repo-path-files repo-root-files)
                build-code-formatter/files-formatted)
      build-exit-codes/catch-all
      build-exit-codes/ok)))
