(ns automaton-build-app.cicd.server
  "Adapter to the CICD
  Proxy to github

  * When run is github action: that environment variable is set automatically, check [docs](https://docs.github.com/en/actions/learn-github-actions/variables)
  * When run is github action container image, we set manually that variable in the `Dockerfile`(clojure/container-images/gha_runner/Dockerfile)
  * Otherwise, that variable is not set and `is-cicd?` returns false"
  (:require [automaton-build-app.os.files :as build-files]
            [clojure.string :as str]
            [automaton-build-app.log :as build-log]))

(def github-env-var "CI")

(defn is-cicd?*
  "Tells if the local instance runs in CICD"
  []
  (boolean (System/getenv github-env-var)))

(def is-cicd? (memoize is-cicd?*))

(defn- update-workflow*
  [filename file-content searched-pattern tag]
  (let [new-content (str/replace file-content searched-pattern (str "$1" tag))]
    (when-not (nil? new-content)
      (build-files/spit-file filename new-content)
      true)))

(defn update-workflow
  "Update a workflow file with
  Params:
  * `filename` filename to modify
  * `container-name` name of the container to update
  * `tag` tag to upsert"
  [filename container-name tag]
  (build-log/info-format "Update file `%s`, with tag `%s`" filename tag)
  (if-let [file-content (build-files/read-file filename)]
    (let [searched-pattern
            (-> (str "(uses:\\s*docker://\\w*/" container-name ")(.*)")
                re-pattern)]
      (if (re-find searched-pattern file-content)
        (update-workflow* filename file-content searched-pattern tag)
        (do (build-log/warn-format
              "Not able to update `%s`, the pattern `%s` has not been found"
              filename
              searched-pattern)
            false)))
    (build-log/warn-format
      "File %s doesn't exist, workflow update is skipped")))

(defn update-workflows
  "Used to update all workflow of a repo to the tag
  Params:
  * `updates` list of updates, each one is a filename and a container
  * `tag` the tag
  * `container-name`"
  [updates tag container-name]
  (doseq [filename updates] (update-workflow filename container-name tag)))

(comment
  (update-workflows [".github/workflows/commit_validation.yml"]
                    "v0.0.8"
                    "gha-automaton-build-app:")
  ;
)
