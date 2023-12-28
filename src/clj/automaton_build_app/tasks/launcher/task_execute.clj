(ns automaton-build-app.tasks.launcher.task-execute
  (:require [automaton-build-app.app.build-config :as build-build-config]
            [automaton-build-app.app.build-config.tasks :as build-config-tasks]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.log.files :as build-log-files]
            [automaton-build-app.os.exit-codes :as build-exit-codes]
            [automaton-build-app.tasks.launcher.app-data :as build-launcher-app-data]
            [automaton-build-app.tasks.launcher.cli-task-opts :as build-tasks-cli-opts]
            [automaton-build-app.tasks.launcher.pf-dispatcher :as build-pf-dispatcher]
            [automaton-build-app.tasks.launcher.task :as build-launcher-task]))

(defn task-execute
  "Execute the task `task-name` with arguments `cli-args` in the application `app`."
  [{:keys [app-dir raw-build-config]
    :as app} task-name cli-args]
  (let [{:keys [task-map task-registry tasks-schema mandatory-tasks]
         :as tasks}
        (->> (build-config-tasks/tasks-names raw-build-config)
             (build-launcher-task/build app-dir task-name))
        cli-opts (build-tasks-cli-opts/cli-opts (:task-cli-opts-kws task-map) cli-args)]
    (if (or (not (build-tasks-cli-opts/are-cli-opts-valid? cli-opts "That arguments are not compatible"))
            (not (build-tasks-cli-opts/mandatory-option-present? cli-opts (:task-cli-opts-kws task-map)))
            (empty? tasks))
      build-exit-codes/invalid-argument
      (let [app (->> (build-config-tasks/update-build-config-tasks raw-build-config mandatory-tasks)
                     (build-build-config/build-config-default-values tasks-schema)
                     (assoc app :build-config))
            app-data (build-launcher-app-data/build app (merge task-map {:task-registry task-registry}) cli-opts cli-args)]
        (build-log-files/save-debug-info "app.edn" app "For debugging only.")
        (build-log-files/save-debug-info "app_data.edn" app-data "For debug only.")
        (cond (nil? app-data) (do (build-log/error-format "No data found for task `%s`" task-name) build-exit-codes/cannot-execute)
              (some? task-map) (build-pf-dispatcher/dispatch task-map app-data cli-opts)
              :else (do (build-log/error-format "The task `%s` is unknown" task-name) build-exit-codes/invalid-argument))))))
