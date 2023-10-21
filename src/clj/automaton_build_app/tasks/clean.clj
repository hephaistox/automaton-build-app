(ns automaton-build-app.tasks.clean
  "Functions to be called to clean the repo"
  (:require [automaton-build-app.cicd.cfg-mgt :as build-cfg-mgt]
            [automaton-build-app.app :as build-app]
            [automaton-build-app.os.files :as build-files]
            [automaton-build-app.log :as build-log]))

(defn clean-hard
  "Clean the repository to the state as it's after being cloned from git server"
  [_opts]
  (-> (build-files/absolutize ".")
      build-cfg-mgt/clean-hard))

(defn clean
  "Clean cache files for compilers to start from scratch"
  [_opts]
  (let [app-data (@build-app/build-app-data_)
        dirs (get-in app-data [:clean :compile-logs-dirs])]
    (build-log/debug-format "The directories `%s` are cleaned" dirs)
    (build-files/delete-files dirs)))
