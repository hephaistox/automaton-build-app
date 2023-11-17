(ns automaton-build-app.tasks.push-local-dir-to-repo
  (:require [automaton-build-app.cicd.cfg-mgt :as build-cfg-mgt]
            [automaton-build-app.os.files :as build-files]
            [automaton-build-app.tasks.launcher.cli-opts :as build-tasks-cli-opts]
            [automaton-build-app.os.edn-utils :as build-edn-utils]
            [automaton-build-app.log :as build-log]))

(defn push-local-dir-to-repo
  "Push the current repository from the local repository

  Use with care, as source of truth is the monorepo commits"
  [cli-opts app _bb-edn-args]
  (if-let [repo (get-in app [:build-config :publication :repo])]
    (if-let [commit-msg (build-tasks-cli-opts/mandatory-option cli-opts [:message])]
      (do (build-log/debug-format "Push local `%s` " commit-msg)
          (let [{:keys [address branch]} repo]
            (build-cfg-mgt/push-local-dir-to-repo "."
                                                  address
                                                  branch
                                                  commit-msg
                                                  commit-msg
                                                  (get-in app [:build-config :publication :major-version]))))
      (when (build-files/is-existing-file? "version.edn")
        (println (format "Current version is `%s`" (build-edn-utils/read-edn "version.edn")))))
    (build-log/warn "Can't push this repository as it is not set up in build_config.edn")))
