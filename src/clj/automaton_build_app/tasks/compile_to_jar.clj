(ns automaton-build-app.tasks.compile-to-jar
  (:require [automaton-build-app.code-helpers.compiler :as build-compiler]
            [automaton-build-app.code-helpers.frontend-compiler :as build-frontend-compiler]
            [automaton-build-app.os.exit-codes :as build-exit-codes]
            [automaton-build-app.log :as build-log]))

(defn- production-shadow-cljs-compilation
  [{:keys [app-dir target-alias]}]
  (if (build-frontend-compiler/is-shadow-project? app-dir)
    (let [res (build-frontend-compiler/compile-target target-alias app-dir)]
      (build-log/info "frontend compiled - starting backend compilation")
      res)
    true))

(defn- production-clj-compilation
  [{:keys [app-name app-dir _aliases deps-edn publication]} env]
  (build-compiler/clj-compiler app-name app-dir deps-edn publication env))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn exec
  "Compile both backend and frontend (if its setup file exists, e.g. `shadow-cljs.edn`)), in production mode"
  [_task-map app-data]
  (when (production-shadow-cljs-compilation app-data)
    (if (production-clj-compilation app-data "prod")
      build-exit-codes/ok
      build-exit-codes/catch-all)))
