(ns automaton-build-app.tasks.vizualise-deps
  (:require [automaton-build-app.doc.vizualise-deps :as build-vizualise-deps]))

(defn vizualise-deps
  [_parsed-cli-opts _app-dir app-data _bb-edn-args]
  (-> app-data
      (get-in [:doc :reports :output-files :deps] "docs/code/deps.svg")
      build-vizualise-deps/vizualize-deps))
