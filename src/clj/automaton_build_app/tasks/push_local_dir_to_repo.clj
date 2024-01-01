(ns automaton-build-app.tasks.push-local-dir-to-repo
  (:require [automaton-build-app.cicd.cfg-mgt :as build-cfg-mgt]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.os.edn-utils :as build-edn-utils]
            [automaton-build-app.os.exit-codes :as build-exit-codes]
            [automaton-build-app.os.files :as build-files]))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn exec
  "Push the current repository from the local repository"
  [_task-map
   {:keys [app-dir publication message]
    :as _app-data}]
  (let [{:keys [repo branch major-version]} publication]
    (when (build-files/is-existing-file? "version.edn")
      (println (format "Current version is `%s`" (build-edn-utils/read-edn "version.edn"))))
    (build-log/debug-format "Push local `%s` " message)
    (if (true? (build-cfg-mgt/push-local-dir-to-repo app-dir repo branch message message major-version))
      build-exit-codes/ok
      build-exit-codes/catch-all)))
