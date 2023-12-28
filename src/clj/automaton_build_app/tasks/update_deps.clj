(ns automaton-build-app.tasks.update-deps
  (:require [automaton-build-app.app-data :as build-app-data]
            [automaton-build-app.code-helpers.update-deps-clj :as build-update-deps-clj]
            [automaton-build-app.os.exit-codes :as build-exit-codes]))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn exec
  "Update the dependencies of the project"
  [_task-map
   {:keys [exclude-libs]
    :as app-data}]
  (let [dirs-to-update (build-app-data/project-root-dirs app-data)]
    (if (nil? (apply build-update-deps-clj/do-update exclude-libs dirs-to-update)) build-exit-codes/ok build-exit-codes/cannot-execute)))
