(ns automaton-build-app.tasks.launcher.launch-on-clj-entry-point
  (:require [automaton-build-app.tasks.launcher.cli-opts :as build-tasks-cli-opts]
            [automaton-build-app.utils.namespace :as build-namespace]
            [clojure.edn :as edn]))

(defn entry-point
  "Entry point for clojure function task
  * `args` are coming from command line-seq
     * it contains a data has been written in a temporary file which name is passed as a parameter

  Design decisions:
  * Data are passed from bb to clj platform through a temporary file, so all limitations and complexity coming from passing data to clojure cli are worked around.

  Params:
  * `args` arguments coming from the clojure cli"
  [{:keys [clj-input-tmp-file]
    :as _arg}]
  (let [{:keys [cli-opts task-fn bb-edn-args app]} (some-> clj-input-tmp-file
                                                           slurp
                                                           edn/read-string)]
    (when-not (build-tasks-cli-opts/do-common-opts cli-opts) (build-namespace/symbol-to-fn-call task-fn cli-opts app bb-edn-args))))
