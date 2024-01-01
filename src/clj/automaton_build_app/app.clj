(ns automaton-build-app.app
  "The application concept gather all description and setup of the application"
  (:require [automaton-build-app.app.build :as build-app-build]
            [automaton-build-app.app.build-config :as build-build-config]
            [automaton-build-app.app.build-config.tasks :as build-config-tasks]
            [automaton-build-app.os.files :as build-files]
            [automaton-build-app.tasks.launcher.task :as build-launcher-task]))

(defn find-apps-paths
  [dir]
  (->> (build-build-config/search-for-build-configs-paths dir)
       (map build-files/extract-path)))

(defn task-app-data
  [dir task-name]
  (let [{:keys [app-dir raw-build-config]
         :as app}
        (build-app-build/build dir)
        {:keys [tasks-schema mandatory-tasks]
         :as _tasks}
        (->> (build-config-tasks/tasks-names raw-build-config)
             (build-launcher-task/build app-dir task-name))]
    (->> (build-config-tasks/update-build-config-tasks raw-build-config mandatory-tasks)
         (build-build-config/build-config-default-values tasks-schema)
         (assoc app :build-config))))

(defn append-app-dir
  "The `paths` in the collection are updated so the `app-dir` is a prefix"
  [app-dir paths]
  (->> paths
       (map (fn [src-item] (build-files/create-dir-path app-dir src-item)))
       sort
       dedupe
       vec))

(defn test-paths
  "Retrive app test paths."
  [{:keys [app-dir]
    :as app}]
  (->> (get-in app [:deps-edn :aliases :common-test :extra-paths])
       (mapv (partial build-files/create-dir-path app-dir))))

(defn get-build-css-filename [app css-key] (get-in app [:build-config :task-shared :publication :frontend css-key]))

(defn lib-path
  "Creates a map where key is app library reference and value is it's local directory"
  [base-dir app]
  (let [k (get-in app [:build-config :task-shared :publication :as-lib])
        v {:local/root (build-files/relativize (:app-dir app) (build-files/absolutize base-dir))}]
    (when k {k v})))
