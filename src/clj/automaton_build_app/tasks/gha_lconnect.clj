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
  [task-arg _app-dir
   {:keys [app-name publication]
    :as app-data} _bb-edn-args]
  (build-log/info "Run and connect to github container locally")
  (let [tag (get-in task-arg [:options :tag])
        container-repo-account (get-in app-data [:container-repo :account])
        {:keys [gha-container]} publication
        {:keys [repo-url workflows repo-branch]} gha-container
        tmp-dir (build-files/create-temp-dir "gha_image")]
    (if (or (nil? repo-url) (nil? workflows))
      (do (build-log/fatal "Parameters are missing  [:publication :gha-cntainer] in `build_config.edn`")
          (System/exit build-exit-codes/catch-all))
      (gha-lconnect* tmp-dir repo-url repo-branch app-name container-repo-account tag))))
