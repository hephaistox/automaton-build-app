(ns automaton-build-app.tasks.mermaid
  (:require [automaton-build-app.doc.mermaid :as build-mermaid]))

(defn mermaid
  [_task-arg _app-dir app-data _bb-edn-args]
  (-> app-data
      (get-in [:doc :archi :dir] "doc/archi/dir")
      build-mermaid/build-all-files))
