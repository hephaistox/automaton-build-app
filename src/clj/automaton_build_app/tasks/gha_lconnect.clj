(ns automaton-build-app.tasks.gha-lconnect
  (:require [automaton-build-app.containers :as build-containers]
            [automaton-build-app.containers.github-action :as build-github-action]
            [automaton-build-app.os.exit-codes :as build-exit-codes]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.os.files :as build-files]
            [automaton-build-app.cicd.cfg-mgt :as build-cfg-mgt]))

(defn- gha-lconnect*
  [tmp-dir repo-url repo-branch app-name container-repo-account tag]
  (if-not (and (build-cfg-mgt/clone-repo-branch tmp-dir repo-url repo-branch)
               (some-> (build-github-action/make-github-action app-name tmp-dir "" container-repo-account tag)
                       build-containers/connect))
    (do (build-log/fatal "Error during gha connection") build-exit-codes/catch-all)
    build-exit-codes/ok))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn exec
  "Task to locally connect to github action"
  [_task-map
   {:keys [account app-name repo-url repo-branch tag]
    :as _app}]
  (build-log/info "Run and connect to github container locally")
  (let [tmp-dir (build-files/create-temp-dir "gha_image")] (gha-lconnect* tmp-dir repo-url repo-branch app-name account tag)))
