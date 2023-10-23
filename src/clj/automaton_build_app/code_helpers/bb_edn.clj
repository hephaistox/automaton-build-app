(ns automaton-build-app.code-helpers.bb-edn
  "Adapter for `bb.edn`"
  (:require [automaton-build-app.os.edn-utils :as build-edn-utils]
            [automaton-build-app.os.files :as build-files]))

(def bb-edn-filename
  "Should not be used externally except in test namespaces"
  "bb.edn")

(defn update-bb-edn
  "Update the `bb-edn` with the mono file with the file parameter, keep :tasks and :init keys and refresh aliases with tasks content
  Params:
  * `bb-edn-dir` name of the dir where the bb.edn is
  * `update-bb-edn-fn` updater function taking bb.edn content as a paramter and returning the new content to save"
  [bb-edn-dir update-bb-edn-fn]
  (let [bb-edn-filename (build-files/create-file-path bb-edn-dir
                                                      bb-edn-filename)
        bb-edn (build-edn-utils/read-edn bb-edn-filename)
        updated-bb-edn (update-bb-edn-fn bb-edn)]
    (build-edn-utils/spit-edn bb-edn-filename updated-bb-edn)))
