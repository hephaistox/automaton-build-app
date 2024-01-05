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
            [automaton-build-app.os.cli-input :as build-cli-input]
            [automaton-build-app.os.edn-utils :as build-edn-utils]
            [clojure.tools.build.api :as clj-build-api]
            [automaton-build-app.os.files :as build-files]))

(def version-file "version.edn")

(defn read-version-file
  [app-dir]
  (let [version-filename (build-files/create-file-path app-dir version-file)]
    (when (build-files/is-existing-file? version-filename) (build-edn-utils/read-edn version-filename))))

(defn save-version-file
  [app-dir content]
  (build-edn-utils/spit-edn (build-files/create-file-path app-dir version-file)
                            content
                            ";;Last generated version, note a failed push consume a number"))

(defn update-version
  "Build the string of the version to be pushed (the next one)
  Params:
  * `app-dir` directory of the version to count
  * `major-version`"
  [app-dir major-version]
  (if major-version
    (let [{_version :version
           older-minor-version :minor-version
           older-major-version :major-version}
          (read-version-file app-dir)
          minor-version (if-not (= older-major-version (format major-version -1))
                          (do (build-log/info "A new major version is detected")
                              (build-log/trace-format "Older major version is `%s`" older-major-version)
                              (build-log/trace-format "Newer major version is `%s`" (format major-version -1))
                              -1)
                          older-minor-version)
          new-minor-version (inc (or minor-version -1))
          major-version-only (format major-version -1)
          new-version (format major-version new-minor-version)]
      (build-log/trace-format "Major version: %s, old minor: %s, new minor %s" major-version older-minor-version minor-version)
      (save-version-file app-dir
                         {:major-version major-version-only
                          :version new-version
                          :minor-version new-minor-version})
      new-version)
    (build-log/warn "Major version is missing")))


(defn current-version [app-dir] (:version (read-version-file app-dir)))

(defn version-from-git-revs-to-push
  "Build the string of the version to be pushed (the next one)
  Params:
  * `dir` directory of the version to count
  * `major-version`"
  [dir major-version]
  (if major-version
    (let [minor-version (-> (clj-build-api/git-count-revs {:dir dir})
                            Integer/parseInt)
          new-minor-version (inc (or minor-version -1))
          new-version (format major-version new-minor-version)]
      new-version)
    (build-log/warn "Major version is missing")))

(def version-to-push "Router to the chosen strategy" update-version)

(defn confirm-version?
  ([force?] (confirm-version? force? ""))
  ([force? project-name]
   (build-cli-input/yes-question
    (format "Your change will affect the main branch of the project `%s`, are you sure you want to continue? y/n" project-name)
    force?)))
