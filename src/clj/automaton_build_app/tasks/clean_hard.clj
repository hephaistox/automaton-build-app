(ns automaton-build-app.tasks.clean-hard
  (:require [automaton-build-app.cicd.cfg-mgt :as build-cfg-mgt]
            [automaton-build-app.os.files :as build-files]))

(defn clean-hard
  "Clean the repository to the state as it's after being cloned from git server"
  [_parsed-cli-opts]
  (-> (build-files/absolutize ".")
      build-cfg-mgt/clean-hard))
