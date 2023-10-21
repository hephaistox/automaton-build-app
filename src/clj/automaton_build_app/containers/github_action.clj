(ns automaton-build-app.containers.github-action
  "Manage the github action containers

  The GithubAction record is gathering:
  * `app-name` the name of the app
  * `container-dir` where the container is stored
  * `image-src` where the image of the container is stored
  * `remote-repo-account` account to connect the repo to"
  (:require [automaton-build-app.code-helpers.deps-edn :as build-deps-edn]
            [automaton-build-app.containers :as build-containers]
            [automaton-build-app.containers.local-engine :as build-local-engine]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.os.files :as build-files]))

(defrecord GithubAction [app-name container-dir app-dir remote-repo-account]
  build-containers/Container
    (container-name [_] (str "gha-" app-name))
    (build [this publish?]
      (let [app-files-to-copy-in-cc-container
              (map (partial build-files/create-file-path app-dir)
                [build-deps-edn/deps-edn "package.json" "shadow-cljs.edn"])
            image-name (build-containers/container-name this)]
        (build-log/debug-format
          "Create githubaction container image `%s` for cust-app `%s`"
          image-name
          app-name)
        (build-local-engine/build-and-push-image
          image-name
          remote-repo-account
          container-dir
          (build-files/create-temp-dir image-name)
          app-files-to-copy-in-cc-container
          publish?)))
    (connect [this]
      (if (build-containers/build this false)
        (build-local-engine/container-interactive
          (build-containers/container-name this)
          app-dir)
        (build-log/warn
          "Connection to the container is skipped as build has failed"))))

(defn make-github-action
  [app-name container-dir app-dir remote-repo-account]
  (->GithubAction app-name container-dir app-dir remote-repo-account))
