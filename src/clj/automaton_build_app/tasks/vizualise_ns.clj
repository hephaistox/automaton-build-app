(ns automaton-build-app.tasks.vizualise-ns
  (:require [automaton-build-app.doc.vizualise-ns :as build-vizualise-ns]))

(defn vizualise-ns
  [_cli-opts app _bb-edn-args]
  (-> app
      (get-in [:build-config :doc :reports :output-files :deps-ns] "docs/code/deps-ns.svg")
      build-vizualise-ns/vizualize-ns))
