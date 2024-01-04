(ns automaton-build-app.tasks.publish-app
  (:require [automaton-build-app.code-helpers.compiler :as build-compiler]
            [automaton-build-app.os.exit-codes :as build-exit-codes]))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn exec
  "Publish your project"
  [_task-map
   {:keys [publication]
    :as _app-data}]
  (let [{:keys [clever-uri]} publication] (if (build-compiler/publish-app clever-uri) build-exit-codes/ok build-exit-codes/catch-all)))
