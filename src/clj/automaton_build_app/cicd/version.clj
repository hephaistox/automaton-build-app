(ns automaton-build-app.cicd.version
  "Version of the current codebase

  Principles:
  * tools api from clojure is providing a technique that is counting how many commits you have in a branch, which its:
     * advantages:
        * as it seems more standard way
        * as you need not to store a specific file for that,
     * disadvantages:
        * files are copied from monorepo to sub projects, count the commit from master branch is consuming time during cloning, as there are already deep history and numerous project. So retrieving only the last commit is fetched, which is uncompatible with this technique
  We have chosen that design since monorepo efficiency is more important, same as minimizing the number of source of truth

  Previous design:
  * major version has to be changed in the major-version in `build_config.edn`
  * the file `version.edn` contains the last pushed version data, and is stored in configuration management, at the root of the project
  * each `bb push` increases the minor version
  * changing the major version is done through"
  (:require [automaton-build-app.log :as build-log]
            [automaton-build-app.os.edn-utils :as build-edn-utils]
            [clojure.tools.build.api :as clj-build-api]
            [automaton-build-app.os.files :as build-files]))

(defn version-from-edn-to-push
  "Build the string of the version to be pushed (the next one)
  Params:
  * `dir` directory of the version to count
  * `major-version`"
  [dir major-version commit-sha]
  (if major-version
    (let [version-filename (build-files/create-file-path dir "version.edn")
          version-map (build-edn-utils/read-edn version-filename)
          {_version :version,
           older-minor-version :minor-version,
           older-major-version :major-version}
            version-map
          minor-version (when (= older-major-version major-version)
                          older-minor-version)
          new-minor-version (inc (or minor-version -1))
          new-version (format major-version new-minor-version)]
      (build-edn-utils/spit-edn
        version-filename
        {:version new-version,
         :major-version major-version,
         :commit commit-sha,
         :minor-version new-minor-version}
        "Last generated version, note a failed push consume a number")
      new-version)
    (build-log/warn "Major version is missing")))

(defn version-from-git-revs-to-push
  "Build the string of the version to be pushed (the next one)
  Params:
  * `dir` directory of the version to count
  * `major-version`"
  [dir major-version _commit-sha]
  (if major-version
    (let [minor-version (-> (clj-build-api/git-count-revs {:dir dir})
                            Integer/parseInt)
          new-minor-version (inc (or minor-version -1))
          new-version (format major-version new-minor-version)]
      new-version)
    (build-log/warn "Major version is missing")))

(def version-to-push "Router to the chosen strategy" version-from-edn-to-push)
