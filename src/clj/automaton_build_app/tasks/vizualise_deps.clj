(ns automaton-build-app.tasks.vizualise-deps
  (:require [automaton-build-app.doc.vizualise-deps :as build-vizualise-deps]))

(defn vizualise-deps
  [_cli-opts app _bb-edn-args]
  (-> app
      (get-in [:build-config :doc :reports :output-files :deps] "docs/code/deps.svg")
      build-vizualise-deps/vizualize-deps))
