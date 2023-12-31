(ns automaton-build-app.tasks.publish-to-clojars
  (:require [automaton-build-app.code-helpers.compiler :as build-compiler]
            [automaton-build-app.os.exit-codes :as build-exit-codes]))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn exec
  "Publish your project"
  [_task-map
   {:keys [publication]
    :as _app-data}]
  (let [{:keys [pom-path jar-path]} publication]
    (if (build-compiler/publish-to-clojars jar-path pom-path) build-exit-codes/ok build-exit-codes/catch-all)))
