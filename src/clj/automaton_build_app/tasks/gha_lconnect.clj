(ns automaton-build-app.tasks.gha-lconnect
  (:require [automaton-build-app.containers :as build-containers]
            [automaton-build-app.containers.github-action :as build-github-action]
            [automaton-build-app.os.exit-codes :as build-exit-codes]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.os.files :as build-files]
            [automaton-build-app.cicd.cfg-mgt :as build-cfg-mgt]))

(defn- gha-lconnect*
  [tmp-dir repo-url repo-branch app-name container-repo-account tag]
  (when-not (and (build-cfg-mgt/clone-repo-branch tmp-dir repo-url repo-branch)
                 (some-> (build-github-action/make-github-action app-name tmp-dir "" container-repo-account tag)
                         build-containers/connect))
    (build-log/fatal "Error during gha connection")
    (System/exit build-exit-codes/catch-all)))

(defn gha-lconnect
  "Task to locally connect to github action"
  [cli-opts
   {:keys [app-name]
    :as app} _bb-edn-args]
  (build-log/info "Run and connect to github container locally")
  (let [tag (get-in cli-opts [:options :tag])
        container-repo-account (get-in app [:container-repo :account])
        {:keys [repo-url workflows repo-branch]} (get-in app [:build-config :publication :gha-container])
        tmp-dir (build-files/create-temp-dir "gha_image")]
    (if (or (nil? repo-url) (nil? workflows))
      (build-log/warn-format "Gha lconnection is skipped, as parameters repo-url=`%s` and workflows=`%s` should not be null"
                             repo-url
                             workflows)
      (gha-lconnect* tmp-dir repo-url repo-branch app-name container-repo-account tag))))
