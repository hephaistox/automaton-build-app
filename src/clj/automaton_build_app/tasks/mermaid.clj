(ns automaton-build-app.tasks.mermaid
  (:require [automaton-build-app.doc.mermaid :as build-mermaid]))

(defn mermaid
  [_cli-opts app _bb-edn-args]
  (-> (get-in app [:build-config :doc :archi :dir] "doc/archi/dir")
      build-mermaid/build-all-files))
