(ns automaton-build-app.tasks.code-helpers-clj
  "Separate namespace from code-helpers so the transitive dependencies of this namespace don't mess upt bb "
  (:require [automaton-build-app.app :as build-app]
            [automaton-build-app.code-helpers.compiler :as build-compiler]
            [automaton-build-app.code-helpers.frontend-compiler :as
             build-frontend-compiler]
            [automaton-build-app.code-helpers.update-deps :as build-update-deps]
            [automaton-build-app.log :as build-log]))

(defn update-deps
  "Update the dependencies of the project"
  [{:keys [min-level], :as _opts}]
  (build-log/set-min-level! min-level)
  (build-update-deps/do-update))

(defn compile-to-jar
  "Compile both backend and frontend (if its setup file exists, e.g. `shadow-cljs.edn`)), in production mode"
  [{:keys [min-level], :as _opts}]
  (build-log/set-min-level! min-level)
  (let [app-dir ""
        {:keys [publication deps-edn]} (@build-app/build-app-data_ "")
        {:keys [as-lib jar major-version]} publication
        {:keys [target-filename class-dir]} jar
        excluded-aliases #{}]
    (when (build-frontend-compiler/is-shadow-project? app-dir)
      (build-frontend-compiler/compile-target :app app-dir))
    (build-compiler/clj-compiler deps-edn
                                 target-filename
                                 as-lib
                                 excluded-aliases
                                 class-dir
                                 major-version)))
