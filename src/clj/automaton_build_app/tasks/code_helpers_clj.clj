(ns automaton-build-app.tasks.code-helpers-clj
  "Separate namespace from code-helpers so the transitive dependencies of this namespace don't mess upt bb "
  (:require [automaton-build-app.app :as build-app]
            [automaton-build-app.code-helpers.compiler :as build-compiler]
            [automaton-build-app.code-helpers.frontend-compiler :as
             build-frontend-compiler]
            [automaton-build-app.code-helpers.update-deps-clj :as
             build-update-deps-clj]
            [automaton-build-app.os.exit-codes :as build-exit-codes]
            [automaton-build-app.log :as build-log]))

(defn update-deps
  "Update the dependencies of the project"
  [{:keys [min-level], :as _parsed-cli-opts}]
  (build-log/set-min-level! min-level)
  (build-update-deps-clj/do-update ""))

(defn compile-to-jar
  "Compile both backend and frontend (if its setup file exists, e.g. `shadow-cljs.edn`)), in production mode"
  [{:keys [min-level], :as _parsed-cli-opts}]
  (build-log/set-min-level! min-level)
  (let [app-dir ""
        {:keys [publication deps-edn]} (@build-app/build-app-data_ app-dir)
        {:keys [as-lib jar major-version shadow-cljs]} publication
        {:keys [excluded-aliases target-filename class-dir]} jar
        {:keys [target-build]} shadow-cljs
        skip-frontend-building?
          (or (not (build-frontend-compiler/is-shadow-project? app-dir))
              (nil? target-build))
        res (and (or skip-frontend-building?
                     (build-frontend-compiler/compile-target target-build
                                                             app-dir))
                 (build-compiler/clj-compiler app-dir
                                              deps-edn
                                              target-filename
                                              as-lib
                                              excluded-aliases
                                              class-dir
                                              major-version))]
    (when-not res (System/exit build-exit-codes/catch-all))
    res))
