(ns automaton-build-app.tasks.vizualise-deps
  (:require [automaton-build-app.doc.vizualise-deps :as build-vizualise-deps]
            [automaton-build-app.os.exit-codes :as build-exit-codes]))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn exec
  [_task-map {:keys [output-file]}]
  (if (build-vizualise-deps/vizualize-deps output-file) build-exit-codes/ok build-exit-codes/catch-all))
