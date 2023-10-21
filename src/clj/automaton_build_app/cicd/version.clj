(ns automaton-build-app.cicd.version
  "Version of the current codebase"
  (:require [automaton-build-app.log :as build-log]
            [automaton-build-app.os.edn-utils :as build-edn-utils]
            [automaton-build-app.os.files :as build-files]))

(defn version-to-push
  "Build the string of the version to be pushed (the next one)
  Params:
  * `dir` directory of the repo to count the version of
  * `major-version`"
  [dir major-version]
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
         :minor-version new-minor-version}
        "Last generated version, note a failed push consume a number")
      new-version)
    (build-log/warn "Major version is missing")))
