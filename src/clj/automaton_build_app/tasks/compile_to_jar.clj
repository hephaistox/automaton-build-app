(ns automaton-build-app.tasks.compile-to-jar
  (:require [automaton-build-app.code-helpers.compiler :as build-compiler]
            [automaton-build-app.code-helpers.frontend-compiler :as build-frontend-compiler]
            [automaton-build-app.os.exit-codes :as build-exit-codes]))

(defn compile-to-jar
  "Compile both backend and frontend (if its setup file exists, e.g. `shadow-cljs.edn`)), in production mode"
  [_task-arg app-dir app-data _bb-edn-args]
  (let [{:keys [publication deps-edn]} app-data
        {:keys [as-lib jar major-version shadow-cljs]} publication
        {:keys [excluded-aliases target-filename class-dir]} jar
        {:keys [target-build]} shadow-cljs
        skip-frontend-building? (or (not (build-frontend-compiler/is-shadow-project? app-dir)) (nil? target-build))
        res (and (or skip-frontend-building? (build-frontend-compiler/compile-target target-build app-dir))
                 (build-compiler/clj-compiler app-dir deps-edn target-filename as-lib excluded-aliases class-dir major-version))]
    (when-not res (System/exit build-exit-codes/catch-all))
    res))
