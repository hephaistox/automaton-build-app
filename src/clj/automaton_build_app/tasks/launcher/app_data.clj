(ns automaton-build-app.tasks.launcher.app-data
  "Before a task execution, gather all data needed by the task to provide its app-data"
  (:require [automaton-build-app.log :as build-log]
            [automaton-build-app.utils.map :as build-utils-map]))

(defn build
  "Prepare the application data
  Data necessary for the task execution are needed, it includes:
  * `build-config` data necessary for `task-name` (specific task configuration and shared ones)
  * `options` option data coming from cli arguments
  * `task-name` the name of the task
  * `app-name` application name
  * `app-dir` application dir
  * `deps-edn` `deps.edn` file content
  * `bb-edn` `bb.edn` file content
  Params:
  * `app`
  * `tasks-data`
  * `cli-opts`
  * `cli-args`"
  [{:keys [build-config]
    :as app}
   {:keys [build-config-task-kws shared task-name task-registry]
    :as _tasks-data} cli-opts cli-args]
  (let [build-config-task-data (-> (get build-config :tasks)
                                   (select-keys (map keyword build-config-task-kws))
                                   vals)
        build-config-shared-data (-> (get build-config :task-shared)
                                     (build-utils-map/select-keys* (map keyword shared)))]
    (if (nil? build-config-task-data)
      (do (build-log/warn-format "`build-config.edn` does not contain any value for a mandatory task [:tasks %s]" task-name) nil)
      (apply merge
             build-config-shared-data
             (:options cli-opts)
             {:task-name task-name
              :task-registry task-registry
              :cli-args cli-args
              :cli-opts cli-opts}
             (select-keys app [:app-name :app-dir :deps-edn :bb-edn])
             build-config-task-data))))
