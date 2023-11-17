(ns automaton-build-app.tasks.gha-container-publish
  (:require [automaton-build-app.cicd.cfg-mgt :as build-cfg-mgt]
            [automaton-build-app.cicd.server :as build-cicd-server]
            [automaton-build-app.containers :as build-containers]
            [automaton-build-app.containers.github-action :as build-github-action]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.os.exit-codes :as build-exit-codes]
            [automaton-build-app.os.files :as build-files]
            [automaton-build-app.tasks.launcher.cli-opts :as build-tasks-cli-opts]))

(defn- push-gha-from-local*
  [container-url container-dir container tag gha-workflows repo-branch]
  (build-cfg-mgt/clone-repo-branch container-dir container-url repo-branch)
  (let [container-name (build-containers/container-name container)]
    (when-not (and container (build-containers/build container true) (build-cicd-server/update-workflows gha-workflows tag container-name))
      (System/exit build-exit-codes/catch-all))))

(defn gha-container-publish
  "Build the container, publish the local code
  The gha container is adapted for each application, so this build is tagged with the name of the app and its version.
  The deps files are copied in the docker to preload all deps (for instance all `deps.edn`)"
  [cli-opts
   {:keys [app-dir app-name]
    :as app} _bb-edn-args]
  (build-log/info "Build and publish github container")
  (let [{:keys [repo-url workflows repo-account repo-branch]} (get-in app [:build-config :publication :gha-container])
        tag (get cli-opts :tag)
        container-dir (build-files/create-temp-dir "gha-container")
        container (build-github-action/make-github-action app-name container-dir app-dir repo-account tag)
        container-name (build-containers/container-name container)]
    (build-cicd-server/show-tag-in-workflows workflows container-name)
    (cond (not (and workflows repo-url)) (build-log/warn "Skipped as build_config.edn parameters are not set")
          (nil? (build-tasks-cli-opts/mandatory-option cli-opts [:tag])) (System/exit build-exit-codes/catch-all)
          :else (push-gha-from-local* repo-url container-dir container tag workflows repo-branch))))
