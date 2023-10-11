(ns automaton-build-app.tasks.clean
  "Tasks to clean the repository"
  (:require
   [automaton-build-app.cicd.cfg-mgt :as build-cfg-mgt]
   [automaton-build-app.os.files :as build-files]
   [automaton-build-app.tasks.common :as build-tasks-common]))

(defn clean-hard
  "Clean the repository to the state as it's after being cloned from git server"
  []
  (-> (build-files/absolutize ".")
      build-cfg-mgt/clean-hard
      last
      build-tasks-common/exit-code))

(defn clean
  "Clean cache files for compilers to start from scratch
  Params:
  * `dirs` is a list of directory to clean (could be relative to project root or absolute)"
  [& dirs]
  (build-files/delete-files dirs))
