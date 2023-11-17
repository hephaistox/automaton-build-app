(ns automaton-build-app.app.build
  "Build the app data, setup and description

  Params:
  * `app-dir` is where all the files will be searched"
  (:require [automaton-build-app.app.bb-edn :as build-bb-edn]
            [automaton-build-app.app.build-config :as build-build-config]
            [automaton-build-app.app.deps-edn :as build-deps-edn]
            [automaton-build-app.code-helpers.frontend-compiler :as build-frontend-compiler]
            [automaton-build-app.log :as build-log]))

(defn build
  "Gather data describing the application stored in `app-dir`
  Params:
  * `app-dir`"
  [app-dir]
  (build-log/debug-format "Build app data based on directory `%s`" app-dir)
  (let [build-config (build-build-config/read-build-config app-dir)
        app-name (get build-config :app-name)]
    {:build-config build-config
     :app-dir app-dir
     :app-name app-name
     :bb-edn (build-bb-edn/read-bb-edn app-dir)
     :shadow-cljs (build-frontend-compiler/load-shadow-cljs app-dir)
     :deps-edn (build-deps-edn/load-deps-edn app-dir)}))
