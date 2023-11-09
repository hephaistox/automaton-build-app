(ns automaton-build-app.tasks.vizualise-ns
  (:require [automaton-build-app.doc.vizualise-ns :as build-vizualise-ns]))

(defn vizualise-ns
  [_task-arg _app-dir app-data _bb-edn-args]
  (-> app-data
      (get-in [:doc :reports :output-files :deps-ns] "docs/code/deps-ns.svg")
      build-vizualise-ns/vizualize-ns))
