(ns automaton-build-app.tasks.workflow.workflow-to-task
  "Create workflow tasks, a juxt-aposition of other existing tasks.
  To do that, workflow needs:
  * A setup of workflows in `automaton-build-app.workflows.registry`
  * Creation of associated tasks in the bb edn, which needs to:
      * Add the tasks in the task registry
      * Creates the sumup of all cli opts for the workflow
      * Deciding if the task should be launched in bb or clj
  * A composer callable from tasks and calling successively the different tasks composing the workflow"
  (:require [automaton-build-app.tasks.registry.find :as build-task-registry-find]
            [automaton-build-app.log :as build-log]))

(defn- workflow-to-task
  "Transform one workflow item to a real task, with all necessary fields set"
  [task-registry
   {:keys [wk-tasks]
    :as workflow-item}]
  (let [tasks-in-workflow (build-task-registry-find/task-selection task-registry wk-tasks)
        specific-cli-opts-kw (mapv :specific-cli-opts-kws tasks-in-workflow)]
    (assoc workflow-item
           :pf (if (every? #(or (nil? %) (= :bb %)) (mapv :pf tasks-in-workflow)) :bb :clj)
           :specific-cli-opts-kw specific-cli-opts-kw)))

(defn update-registry-workflow-entries
  "Transform the workflow task in the registry to a classical bb-tasks registry

  Calculate which pf to run (:clj if any wk-tasks is :clj, :bb otherwise)
  And the aggregation of cli options"
  [task-registry]
  (->> task-registry
       (map (fn [[task-name
                  {:keys [wk-tasks specific-cli-opts-kws]
                   :as workflow-item}]]
              (when specific-cli-opts-kws
                (build-log/warn-format
                 "Cli options have been found on workflow task %s, it will be ignored, the ones of the wk-tasks will be used instead"))
              [task-name (if wk-tasks (workflow-to-task task-registry workflow-item) workflow-item)]))))
