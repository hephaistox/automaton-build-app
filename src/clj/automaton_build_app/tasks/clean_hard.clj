(ns automaton-build-app.tasks.clean-hard
  (:require [automaton-build-app.cicd.cfg-mgt :as build-cfg-mgt]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.os.files :as build-files]))

(defn clean-hard
  "Clean the repository to the state as it's after being cloned from git server"
  [{:keys [min-level details]
    :as _parsed-cli-opts}]
  (build-log/set-min-level! min-level)
  (build-log/set-details? details)
  (-> (build-files/absolutize ".")
      build-cfg-mgt/clean-hard))
