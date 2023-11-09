(ns automaton-build-app.tasks.docstring
  (:require [automaton-build-app.app :as build-app]
            [automaton-build-app.doc.docstring :as build-docstring]
            [automaton-build-app.log :as build-log]))

(defn doc-string
  [_parsed-cli-opts app-dir app-data _bb-edn-args]
  (let [{:keys [app-name]} app-data
        code-doc (get-in app-data [:doc :code-doc] {})
        {:keys [title description dir]} code-doc
        app-dirs (build-app/src-dirs app-data)]
    (if (empty? code-doc)
      (do (build-log/debug "doc string doc generation is skipped as no parameters are found") true)
      (build-docstring/docstring app-dir app-name app-dirs title description dir))))
