(ns automaton-build-app.tasks.compile-to-jar
  (:require [automaton-build-app.code-helpers.compiler :as build-compiler]
            [automaton-build-app.code-helpers.frontend-compiler :as build-frontend-compiler]
            [automaton-build-app.os.exit-codes :as build-exit-codes]
            [automaton-build-app.log :as build-log]))

(defn- production-shadow-cljs-compilation
  [{:keys [app-dir]
    :as app}]
  (let [{:keys [target-build]} (get-in [:build-config :publication :shadow-cljs] app)]
    (if (or (build-frontend-compiler/is-shadow-project? app-dir) (some? target-build))
      (build-frontend-compiler/compile-target target-build app-dir)
      (build-log/warn "cljs compilation is skipped as setup not found"))))

(defn- production-clj-compilation
  [{:keys [app-dir deps-edn]
    :as app}]
  (let [{:keys [as-lib jar major-version]} (get-in app [:build-config :publication])
        {:keys [excluded-aliases target-filename class-dir]} jar]
    (if (some? target-filename)
      (build-compiler/clj-compiler app-dir deps-edn target-filename as-lib excluded-aliases class-dir major-version)
      (build-log/warn "clj compilation is skipped as setup not found"))))

(defn compile-to-jar
  "Compile both backend and frontend (if its setup file exists, e.g. `shadow-cljs.edn`)), in production mode"
  [_task-arg app _bb-edn-args]
  (production-shadow-cljs-compilation app)
  (when-not (production-clj-compilation app) (System/exit build-exit-codes/catch-all)))
