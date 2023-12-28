(ns automaton-build-app.tasks.vizualise-ns
  (:require [automaton-build-app.doc.vizualise-ns :as build-vizualise-ns]
            [automaton-build-app.os.exit-codes :as build-exit-codes]))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn exec
  [_task-map {:keys [output-file]}]
  (if-not (build-vizualise-ns/vizualize-ns output-file) build-exit-codes/catch-all build-exit-codes/ok))
