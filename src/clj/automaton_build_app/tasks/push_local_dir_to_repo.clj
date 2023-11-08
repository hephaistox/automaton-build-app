(ns automaton-build-app.tasks.push-local-dir-to-repo
  (:require [automaton-build-app.app :as build-app]
            [automaton-build-app.code-helpers.formatter :as build-code-formatter]
            [automaton-build-app.cicd.cfg-mgt :as build-cfg-mgt]
            [automaton-build-app.os.files :as build-files]
            [automaton-build-app.os.edn-utils :as build-edn-utils]
            [automaton-build-app.log :as build-log]))

(defn push-local-dir-to-repo
  "Push the current repository from the local repository

  Use with care, as source of truth is the monorepo commits"
  [task-arg _app-dir app-data _bb-edn-args]
  (if-let [commit-msg (get-in task-arg [:options :message])]
    (let [_ (build-log/debug-format "Push local `%s` " commit-msg)
          src-paths (build-app/src-dirs app-data)
          repo (get-in app-data [:publication :repo])
          {:keys [address branch]} repo]
      (apply build-code-formatter/format-all-app src-paths)
      (build-cfg-mgt/push-local-dir-to-repo "." address branch commit-msg commit-msg (get-in app-data [:publication :major-version])))
    (do (when (build-files/is-existing-file? "version.edn")
          (println (format "Current version is `%s`" (build-edn-utils/read-edn "version.edn"))))
        (build-log/error (get-in task-arg [:summary])))))
