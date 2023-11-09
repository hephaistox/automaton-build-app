(ns automaton-build-app.tasks.launcher.launch-on-clj-entry-point
  (:require [automaton-build-app.app :as build-app]
            [automaton-build-app.os.edn-utils :as build-edn-utils]
            [automaton-build-app.tasks.launcher.cli-opts :as build-tasks-cli-opts]
            [automaton-build-app.utils.namespace :as build-namespace]))

(defn entry-point
  "Entry point for clojure function task
  args are coming from command line-seq
  the task-fn is called in that environment
  Data have been written in a temporary file which name is passed as a parameter
  So all limitations and complexity coming from passing data to clojure cli are worked around.

  Params:
  * `clj-cli-args` arguments coming from the clojure cli"
  [{:keys [clj-input-tmp-file]
    :as _arg}]
  (let [parsed-clj-cli-opts (build-edn-utils/read-edn clj-input-tmp-file)
        {:keys [cli-opts task-fn app-dir bb-edn-args]} parsed-clj-cli-opts]
    (when-not (build-tasks-cli-opts/do-common-opts cli-opts)
      (build-namespace/symbol-to-fn-call task-fn cli-opts app-dir (build-app/build-app-data app-dir) bb-edn-args))))
