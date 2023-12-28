(ns automaton-build-app.tasks.mermaid-watch
  (:require [automaton-build-app.doc.mermaid :as build-mermaid]
            [automaton-build-app.os.exit-codes :as build-exit-codes]))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn exec [_task-map {:keys [mermaid-dir]}] (if (nil? (build-mermaid/watch mermaid-dir)) build-exit-codes/ok build-exit-codes/catch-all))
