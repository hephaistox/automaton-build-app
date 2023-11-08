(ns automaton-build-app.code-helpers.bb-edn
  "Adapter for `bb.edn`"
  (:require [automaton-build-app.os.edn-utils :as build-edn-utils]
            [automaton-build-app.os.files :as build-files]
            [automaton-build-app.log :as build-log]))

(def bb-edn-filename "Should not be used externally except in test namespaces" "bb.edn")

(defn bb-edn-filename-fullpath "Return the full path of the bb.edn file" [app-dir] (build-files/create-file-path app-dir bb-edn-filename))

(defn read-bb-edn
  "Returns the bb-edn file content"
  [app-dir]
  (let [bb-edn-filename-fullpath (bb-edn-filename-fullpath app-dir)
        bb-edn (build-edn-utils/read-edn bb-edn-filename-fullpath)]
    (if (and bb-edn-filename-fullpath bb-edn)
      bb-edn
      (build-log/error-format "Are you sure directory `%s` is an app, no valid bb task in it"))))

(defn update-bb-edn
  "Update the `bb-edn` with the mono file with the file parameter, keep :tasks and :init keys and refresh aliases with tasks content
  Returns true iif the file has changed

  Params:
  * `app-dir` name of the dir where the bb.edn is
  * `update-bb-edn-fn` updater function taking bb.edn content as a paramter and returning the new content to save"
  [app-dir update-bb-edn-fn]
  (let [bb-edn-filename-fullpath (bb-edn-filename-fullpath app-dir)
        bb-edn-content (read-bb-edn app-dir)
        updated-bb-edn (-> bb-edn-content
                           update-bb-edn-fn)]
    (if (= updated-bb-edn bb-edn-content) bb-edn-content (do (build-edn-utils/spit-edn bb-edn-filename-fullpath updated-bb-edn) false))))
