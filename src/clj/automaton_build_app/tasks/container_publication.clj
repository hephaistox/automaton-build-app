(ns automaton-build-app.tasks.container-publication
  "Tasks to publish a container"
  (:require [automaton-build-app.app :as build-app]
            [automaton-build-app.cicd.cfg-mgt :as build-cfg-mgt]
            [automaton-build-app.cicd.server :as build-server]
            [automaton-build-app.containers :as build-containers]
            [automaton-build-app.containers.github-action :as
             build-github-action]
            [automaton-build-app.containers.local-engine :as build-local-engine]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.os.exit-codes :as build-exit-codes]
            [automaton-build-app.os.files :as build-files]
            [clojure.string :as str]))

(defn- push-gha-from-local*
  [container-url container-dir app-name container-repo-account tag gha-workflows
   repo-branch]
  (build-cfg-mgt/clone-repo-branch container-dir container-url repo-branch)
  (let [container (build-github-action/make-github-action app-name
                                                          container-dir
                                                          ""
                                                          container-repo-account
                                                          tag)]
    (when-not (and container
                   (build-containers/build container true)
                   (build-server/update-workflows gha-workflows tag))
      (System/exit build-exit-codes/catch-all))))

(defn push-gha-from-local
  "Build the container, publish the local code
  The gha container is adapted for each application, so this build is tagged with the name of the app and its version.
  The deps files are copied in the docker to preload all deps (for instance all `deps.edn`)"
  [opts]
  (build-log/info "Build and publish github container")
  (let [{:keys [cli-opts]} opts
        tag (get-in cli-opts [:options :tag])
        {:keys [app-name publication]} (@build-app/build-app-data_ "")
        gha-repo-url (get-in publication [:gha-container :repo-url])
        gha-workflows (get-in publication [:gha-container :workflows])
        gha-repo-account (get-in publication [:gha-container :account])
        gha-repo-branch (get-in publication [:gha-container :repo-branch])
        container-dir (build-files/create-temp-dir "gha-container")]
    (cond (str/blank? tag) (do (build-log/error-format
                                 "Cli option are missing, check below")
                               (println (get-in cli-opts [:summary])))
          (not (and gha-workflows gha-repo-url))
            (build-log/warn
              "Skipped as build_config.edn parameters are not set")
          :else (push-gha-from-local* gha-repo-url
                                      container-dir
                                      app-name
                                      gha-repo-account
                                      tag
                                      gha-workflows
                                      gha-repo-branch))))

(defn container-list
  "List all available containers"
  [_opts]
  (println (build-local-engine/container-image-list)))

(defn container-clean
  [_opts]
  (build-log/info "Clean the containers")
  (build-local-engine/container-clean))
