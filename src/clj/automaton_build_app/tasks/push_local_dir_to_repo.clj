(ns automaton-build-app.tasks.push-local-dir-to-repo
  (:require [automaton-build-app.cicd.cfg-mgt :as build-cfg-mgt]
            [automaton-build-app.os.files :as build-files]
            [automaton-build-app.tasks.launcher.cli-opts :as build-tasks-cli-opts]
            [automaton-build-app.os.edn-utils :as build-edn-utils]
            [automaton-build-app.log :as build-log]))

(defn push-local-dir-to-repo
  "Push the current repository from the local repository

  Use with care, as source of truth is the monorepo commits"
  [task-arg _app-dir app-data _bb-edn-args]
  (if-let [commit-msg (build-tasks-cli-opts/mandatory-option task-arg [:message])]
    (let [_ (build-log/debug-format "Push local `%s` " commit-msg)
          repo (get-in app-data [:publication :repo])
          {:keys [address branch]} repo]
      (build-cfg-mgt/push-local-dir-to-repo "." address branch commit-msg commit-msg (get-in app-data [:publication :major-version])))
    (when (build-files/is-existing-file? "version.edn")
      (println (format "Current version is `%s`" (build-edn-utils/read-edn "version.edn"))))))
