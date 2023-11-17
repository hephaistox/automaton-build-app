(ns automaton-build-app.tasks.la
  (:require [automaton-build-app.la :as build-la]))

(defn la
  "Local acceptance"
  [task-cli-opts
   {:keys [app-dir]
    :as _app} _bb-edn-args]
  (build-la/run app-dir task-cli-opts))
