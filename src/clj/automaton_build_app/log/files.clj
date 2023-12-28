(ns automaton-build-app.log.files
  "To use when logging needs to be done to a file."
  (:require [automaton-build-app.os.edn-utils :as build-edn-utils]
            [automaton-build-app.os.files :as build-files]))

(defn save-debug-info
  ([filename content header] (build-edn-utils/spit-edn (build-files/create-file-path "tmp" filename) content header))
  ([filename content] (save-debug-info filename content nil)))
