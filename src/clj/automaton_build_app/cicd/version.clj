(ns automaton-build-app.cicd.version
  "Version of the current codebase"
  (:require [automaton-build-app.log :as build-log]
            [automaton-build-app.os.files :as build-files]))

(def dir "")
(def version-filename (build-files/create-file-path dir "version.edn"))

(defn version-to-push
  "Build the string of the version to be pushed (the next one)
  Params:
  * `dir` directory of the repo to count the version of
  * `major-version`"
  [dir major-version]
  (if major-version
    (let [version-filename (build-files/create-file-path dir "version.edn")
          minor-version (-> version-filename
                            build-files/read-file
                            (or -1)
                            inc)
          version (format major-version minor-version)]
      (build-files/spit-file version-filename version)
      version)
    (build-log/warn "Major version is missing")))
