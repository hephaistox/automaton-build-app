(ns automaton-build-app.app
  "The application concept gather all description and setup of the application"
  (:require [automaton-build-app.app.build :as build-app-build]
            [automaton-build-app.app.impl :as build-app-impl]
            [automaton-build-app.app.bb-edn.task-updater :as bb-edn-task-updater]
            [automaton-build-app.app.bb-edn.deps-updater :as bb-edn-deps-updater]
            [automaton-build-app.app.init :as app-init]
            [automaton-build-app.app.bb-edn :as build-app-bb-edn]))

(defn init! [] (app-init/init!))

(defn build
  "Build the map of the application `build_config.edn`
  Params:
  * `app-dir` the directory path of the application"
  [app-dir]
  (build-app-build/build app-dir))

(defn classpath-dirs
  "Existing source directories for front and back, as strings of absolutized directories
  Params:
  * `app` is the app to get dir from"
  [app]
  (->> (concat (build-app-impl/clj-compiler-classpath app) (build-app-impl/cljs-compiler-classpaths app))
       sort
       dedupe
       vec))

(defn src-dirs
  "Existing source directories for front and back, as strings of absolutized directories
  Exclude resources
  Params:
  * `app` is the app to get dir from"
  [app]
  (->> (concat (build-app-impl/clj-compiler-classpath app) (build-app-impl/cljs-compiler-classpaths app))
       sort
       dedupe
       vec))

(defn update-app
  "Update the bb.edn file with the task registry"
  [task-registry app]
  (let [{:keys [select-tasks exclude-tasks]} (get-in app [:build-config :bb-tasks])]
    (-> app
        (bb-edn-task-updater/update-bb-tasks task-registry select-tasks exclude-tasks)
        bb-edn-deps-updater/update-bb-deps)))

(defn save
  "Save the `bb.edn` `build_config.edn` and `deps-edn`files
  * based on the content of the app
  * only if necessary"
  [app]
  (build-app-bb-edn/spit-if-needed app)
  app)
