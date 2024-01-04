(ns automaton-build-app.tasks.push-local-dir-to-repo
  (:require [automaton-build-app.cicd.cfg-mgt :as build-cfg-mgt]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.os.exit-codes :as build-exit-codes]
            [automaton-build-app.cicd.version :as build-version]))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn exec
  "Push the current repository from the local repository"
  [_task-map
   {:keys [app-dir publication message]
    :as _app-data}]
  (let [{:keys [repo branch major-version]} publication]
    (when-let [version
               (if (= (build-cfg-mgt/current-branch ".") branch)
                 (build-version/version-to-push app-dir major-version)
                 (build-version/current-version app-dir))]
      (build-log/info-format "Current version is `%s`" version)
      (build-log/debug-format "Push local `%s` " message)
      (if (true? (build-cfg-mgt/push-local-dir-to-repo app-dir repo branch message version message))
        build-exit-codes/ok
        build-exit-codes/catch-all))))
