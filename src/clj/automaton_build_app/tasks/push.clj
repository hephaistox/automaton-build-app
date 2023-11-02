(ns automaton-build-app.tasks.push
  (:require [automaton-build-app.app :as build-app]
            [automaton-build-app.code-helpers.formatter :as build-code-formatter]
            [automaton-build-app.cicd.cfg-mgt :as build-cfg-mgt]
            [automaton-build-app.os.files :as build-files]
            [automaton-build-app.os.edn-utils :as build-edn-utils]
            [automaton-build-app.log :as build-log]))

(defn push
  "Push the current repository from the local repository

  Use with care, as source of truth is the monorepo commits"
  [{:keys [min-level]
    :as parsed-cli-opts}]
  (build-log/set-min-level! min-level)
  (if-let [commit-msg (get-in parsed-cli-opts [:cli-opts :options :message])]
    (let [_ (build-log/debug-format "Push local `%s` " commit-msg)
          app-data (@build-app/build-app-data_ "")
          src-paths (build-app/src-dirs app-data)
          repo (get-in app-data [:publication :repo])
          {:keys [address branch]} repo]
      (apply build-code-formatter/format-all-app src-paths)
      (build-cfg-mgt/push-local-dir-to-repo "." address branch commit-msg commit-msg (get-in app-data [:publication :major-version])))
    (let [usage-msg (get-in parsed-cli-opts [:cli-opts :usage-msg])]
      (when (build-files/is-existing-file? "version.edn")
        (println (format "Current version is `%s`" (build-edn-utils/read-edn "version.edn"))))
      (println usage-msg)
      (println (get-in parsed-cli-opts [:cli-opts :summary])))))
