(ns automaton-build-app.app.bb-edn
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
  "Update the `bb-edn` content with the mono file with the file parameter, keep :tasks and :init keys and refresh aliases with tasks content
  Returns unchanged `app`

  Params:
  * `app`"
  [{:keys [app-dir bb-edn]
    :as app}]
  (build-edn-utils/spit-edn (bb-edn-filename-fullpath app-dir) bb-edn "The file is updated automatically")
  app)

(defn tasks
  "Return the tasks from the bb-edn file in parameter"
  [bb-edn]
  (->> (:tasks bb-edn)
       keys
       (remove #(keyword? %))
       sort
       vec))

(comment
  (-> (read-bb-edn "")
      tasks)
  ;
)
