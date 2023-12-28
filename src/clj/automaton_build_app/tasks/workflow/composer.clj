(ns automaton-build-app.tasks.workflow.composer
  "Compose (i.e. call successively) different tasks"
  (:require [automaton-build-app.log :as build-log]
            [automaton-build-app.utils.namespace :as build-namespace]
            [automaton-build-app.tasks.registry.find :as build-task-registry-find]
            [automaton-build-app.os.exit-codes :as build-exit-codes]))

(defn- execute-task-in-wf
  "In a workflow, execute one task - one step
  Returns
  * `nil` if ok
  * `exit-code` if the execution occur
  Params:
  * `task-registry`
  * `cli-opts`
  * `wk-task`"
  [task-registry app cli-opts wk-task]
  (let [{:keys [task-fn]} (build-task-registry-find/task-map task-registry wk-task)]
    (if (nil? task-fn)
      (do (build-log/warn-format "The task `%s` has been skipped, it has not been found in the registry" wk-task) nil)
      (try (build-namespace/symbol-to-fn-call task-fn cli-opts app)
           (catch Exception e
             (build-log/warn-format "Unable to execute fn `%s` with args `%s`" task-fn [cli-opts app wk-task])
             (build-log/error-exception e)
             build-exit-codes/fatal-error-signal)))))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn composer
  "Compose different tasks together
  Params:
  * `cli-opts`
  * `app`"
  [cli-opts
   {:keys [task-registry task-name]
    :as app}]
  (let [{:keys [wk-tasks]} (build-task-registry-find/task-map task-registry task-name)]
    (loop [wk-tasks wk-tasks]
      (let [wk-task (first wk-tasks)
            rest-tasks (rest wk-tasks)
            res (execute-task-in-wf task-registry app cli-opts wk-task)]
        (cond (and (or (nil? res) (= res build-exit-codes/ok)) (seq rest-tasks)) (recur rest-tasks)
              :else res)))))
