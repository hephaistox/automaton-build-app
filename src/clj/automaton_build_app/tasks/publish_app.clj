(ns automaton-build-app.tasks.publish-app
  (:require [automaton-build-app.code-helpers.compiler :as build-compiler]))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn exec
  "Publish project as a runnable app (uber-jar) to clever cloud."
  [_task-map
   {:keys [publication]
    :as _app-data}]
  (let [{:keys [clever-uri]} publication] (build-compiler/publish-app clever-uri)))
