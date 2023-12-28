(ns automaton-build-app.tasks.launcher.launch-on-clj-entry-point
  (:require [automaton-build-app.tasks.launcher.cli-task-agnostic-opts :as build-cli-task-agnostic-opts]
            [automaton-build-app.utils.namespace :as build-namespace]
            [clojure.edn :as edn]
            [automaton-build-app.os.files :as build-files]))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn entry-point
  "Entry point for clojure function task
  * `args` are coming from command line-seq
     * it contains a data has been written in a temporary file which name is passed as a parameter

  Design decisions:
  * Data are passed from bb to clj platform through a temporary file, so all limitations and complexity coming from passing data to clojure cli are worked around.

  Params:
  * `args` arguments coming from the clojure cli"
  [{:keys [clj-input-tmp-file]
    :as _args}]
  (let [{:keys [task-map cli-args app-data]} (some-> clj-input-tmp-file
                                                     build-files/read-file
                                                     edn/read-string)
        {:keys [task-name]} task-map]
    (build-cli-task-agnostic-opts/common-opts! cli-args task-name)
    (build-namespace/symbol-to-fn-call (:task-fn task-map) task-map app-data)))
