(ns automaton-build-app.tasks.la
  (:require [automaton-build-app.code-helpers.bb-edn :as build-bb-edn]
            [automaton-build-app.la :as build-la]
            [automaton-build-app.log :as build-log]))

(defn la
  "Local acceptance"
  [{:keys [min-level]
    :as parsed-cli-args}]
  (let [task-names-in-bb (build-bb-edn/task-names "")]
    (build-log/set-min-level! min-level)
    (build-log/trace-format "The following tasks are found in `bb.edn`: %s" task-names-in-bb)
    (build-la/run task-names-in-bb (get-in parsed-cli-args [:command-line-args]) {})))
