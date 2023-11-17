(ns automaton-build-app.tasks.update-deps
  (:require [automaton-build-app.code-helpers.update-deps-clj :as build-update-deps-clj]))

(defn update-deps
  "Update the dependencies of the project"
  [_cli-opts
   {:keys [app-dir]
    :as _app} _bb-edn-args]
  (build-update-deps-clj/do-update app-dir))
