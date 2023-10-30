(ns automaton-build-app.tasks.clean
  (:require [automaton-build-app.app :as build-app]
            [automaton-build-app.os.files :as build-files]
            [automaton-build-app.log :as build-log]))

(defn clean
  "Clean cache files for compilers to start from scratch"
  [_parsed-cli-opts]
  (let [app-data (@build-app/build-app-data_)
        dirs (get-in app-data [:clean :compile-logs-dirs])]
    (build-log/debug-format "The directories `%s` are cleaned" dirs)
    (build-files/delete-files dirs)))
