(ns automaton-build-app.tasks.compile-app
  (:require [automaton-build-app.code-helpers.compiler :as build-compiler]
            [automaton-build-app.os.exit-codes :as build-exit-codes]
            [automaton-build-app.code-helpers.frontend-compiler :as build-frontend-compiler]
            [automaton-build-app.log :as build-log]))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn exec
  "Compile both backend and frontend (if its setup file exists, e.g. `shadow-cljs.edn`)), in production mode"
  [_task-map {:keys [app-name app-dir _aliases deps-edn publication]}]
  (build-log/info "Start of uberjar compilation")
  (if (and (build-frontend-compiler/is-shadow-project? app-dir) (get-in publication [:frontend :deploy-alias]))
    (let [target-alias (get-in publication [:frontend :deploy-alias])
          res (build-frontend-compiler/compile-release target-alias app-dir)]
      (build-log/info "frontend compiled - starting backend compilation")
      res)
    (build-log/info "Frontend compilation skipped"))
  (if (build-compiler/app-clj-compiler app-name app-dir deps-edn publication "prod") build-exit-codes/ok build-exit-codes/catch-all))
