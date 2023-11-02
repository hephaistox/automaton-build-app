(ns automaton-build-app.tasks.gha-container-publish
  (:require [automaton-build-app.app :as build-app]
            [automaton-build-app.cicd.cfg-mgt :as build-cfg-mgt]
            [automaton-build-app.cicd.server :as build-cicd-server]
            [automaton-build-app.containers :as build-containers]
            [automaton-build-app.containers.github-action :as build-github-action]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.os.exit-codes :as build-exit-codes]
            [automaton-build-app.os.files :as build-files]
            [clojure.string :as str]))

(defn- build-container
  [app-dir app-name container-repo-account container-dir tag]
  (build-github-action/make-github-action app-name container-dir app-dir container-repo-account tag))

(defn- push-gha-from-local*
  [container-url container-dir container tag gha-workflows repo-branch]
  (build-cfg-mgt/clone-repo-branch container-dir container-url repo-branch)
  (let [container-root (build-containers/container-root container)]
    (when-not (and container (build-containers/build container true) (build-cicd-server/update-workflows gha-workflows tag container-root))
      (System/exit build-exit-codes/catch-all))))

(defn gha-container-publish
  "Build the container, publish the local code
  The gha container is adapted for each application, so this build is tagged with the name of the app and its version.
  The deps files are copied in the docker to preload all deps (for instance all `deps.edn`)"
  [{:keys [min-level details]
    :as parsed-cli-opts}]
  (build-log/set-min-level! min-level)
  (build-log/set-details? details)
  (build-log/info "Build and publish github container")
  (let [app-dir ""
        {:keys [cli-opts]} parsed-cli-opts
        tag (get-in cli-opts [:options :tag])
        {:keys [app-name publication]} (@build-app/build-app-data_ app-dir)
        gha-repo-url (get-in publication [:gha-container :repo-url])
        gha-workflows (get-in publication [:gha-container :workflows])
        gha-repo-account (get-in publication [:gha-container :account])
        gha-repo-branch (get-in publication [:gha-container :repo-branch])
        gha-container-dir (build-files/create-temp-dir "gha-container")
        container (build-container app-dir app-name gha-repo-account gha-container-dir tag)
        container-root (build-containers/container-root container)]
    (build-cicd-server/show-tag-in-workflows gha-workflows container-root)
    (cond (str/blank? tag) (do (build-log/error-format "Cli options are missing, check below") (println (get-in cli-opts [:summary])))
          (not (and gha-workflows gha-repo-url)) (build-log/warn "Skipped as build_config.edn parameters are not set")
          :else (push-gha-from-local* gha-repo-url gha-container-dir container tag gha-workflows gha-repo-branch))))
