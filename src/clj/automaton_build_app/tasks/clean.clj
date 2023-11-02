(ns automaton-build-app.tasks.clean
  (:require [automaton-build-app.app :as build-app]
            [automaton-build-app.os.files :as build-files]
            [automaton-build-app.log :as build-log]))

(defn clean
  "Clean cache files for compilers to start from scratch"
  [{:keys [min-level details]
    :as _parsed-cli-opts}]
  (build-log/set-min-level! min-level)
  (build-log/set-details? details)
  (let [app-data (@build-app/build-app-data_)
        dirs (get-in app-data [:clean :compile-logs-dirs])]
    (build-log/debug-format "The directories `%s` are cleaned" dirs)
    (build-files/delete-files dirs)))
