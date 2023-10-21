(ns automaton-build-app.cicd.version
  "Version of the current codebase"
  (:require [automaton-build-app.log :as build-log]
            [clojure.tools.build.api :as build-build-api]))

(defn version-to-push
  "Build the string of the version to be pushed (the next one)
  Params:
  * `dir` directory where to count the git count of commits
  * `major-version`"
  [dir major-version]
  (if major-version
    (->> (build-build-api/git-count-revs {:dir dir})
         Integer/parseInt
         inc
         (format major-version))
    (build-log/warn "Major version is missing")))
