(ns automaton-build-app.tasks.workflow.composer
  "Compose (i.e. call successively) different tasks"
  (:require [automaton-build-app.log :as build-log]
            [automaton-build-app.utils.namespace :as build-namespace]
            [automaton-build-app.tasks.registry.global :as build-task-registry-global]
            [automaton-build-app.tasks.registry.find :as build-task-registry-find]))

(defn composer
  "Compose different tasks together
  Params:
  * `cli_opts`
  * `app-dir`
  * `app-data`
  * `bb-edn-args` contains the list of bb edn to execute"
  [cli-opts app-dir app-data bb-edn-args]
  (let [task-map-registry (build-task-registry-global/build app-dir)]
    (doseq [task-name bb-edn-args]
      (let [{:keys [task-fn]} (build-task-registry-find/find-one task-map-registry task-name)]
        (if (nil? task-fn)
          (build-log/warn-format "The task `%s` has been skipped, it has not been found in the registry" task-name)
          (build-namespace/symbol-to-fn-call task-fn cli-opts app-dir app-data bb-edn-args))))))
