(ns automaton-build-app.app
  "The application concept gather all description and setup of the application"
  (:require [automaton-build-app.app.bb-edn :as build-bb-edn]
            [automaton-build-app.app.bb-edn.deps-updater :as bb-edn-deps-updater]
            [automaton-build-app.app.build :as build-app-build]
            [automaton-build-app.app.build-config :as build-build-config]
            [automaton-build-app.app.build-config.tasks :as build-config-tasks]
            [automaton-build-app.os.files :as build-files]
            [automaton-build-app.tasks.launcher.task :as build-launcher-task]))

(defn prepare-build-config
  [raw-build-config tasks-schema mandatory-tasks]
  (->> (get raw-build-config :tasks)
       (merge (build-config-tasks/task-names->tasks-map mandatory-tasks))
       (assoc raw-build-config :tasks)
       (build-build-config/build-config-default-values tasks-schema)))

(defn find-apps-paths
  [dir]
  (->> (build-build-config/search-for-build-configs-paths dir)
       (map build-files/extract-path)))

(defn prepare-app-data
  [dir task-name]
  (let [{:keys [app-dir raw-build-config]
         :as app}
        (->> (build-app-build/build dir)
             bb-edn-deps-updater/update-bb-deps
             build-bb-edn/update-bb-edn)
        {:keys [tasks-schema mandatory-tasks]
         :as _tasks}
        (->> (build-config-tasks/tasks-names raw-build-config)
             (build-launcher-task/build app-dir task-name))]
    (->> (build-config-tasks/update-build-config-tasks raw-build-config mandatory-tasks)
         (build-build-config/build-config-default-values tasks-schema)
         (assoc app :build-config))))
