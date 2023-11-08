(ns automaton-build-app.tasks.la
  (:require [automaton-build-app.la :as build-la]))

(defn la "Local acceptance" [task-arg app-dir _app-data _bb-edn-args] (build-la/run app-dir task-arg))
