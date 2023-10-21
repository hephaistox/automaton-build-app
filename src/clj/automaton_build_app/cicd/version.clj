(ns automaton-build-app.cicd.version
  "Version of the current codebase"
  (:require [automaton-build-app.log :as build-log]
            [clojure.tools.build.api :as build-build-api]))

(defn version-to-push
  "Build the string of the version to be pushed (the next one)
  Params:
  * `major-version`"
  [major-version]
  (if major-version
    (->> (build-build-api/git-count-revs nil)
         Integer/parseInt
         inc
         (format major-version))
    (build-log/warn "Major version is missing")))
