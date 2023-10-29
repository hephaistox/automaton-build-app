(ns automaton-build-app.la
  (:require [automaton-build-app.bb-tasks :as build-bb-tasks]
            [automaton-build-app.cli-test :as build-cli-test]
            [automaton-build-app.log :as build-log]))

(defn run
  "Defines tests to run
  Params:
  * `tasks-in-app` all the tasks in the application, are symbols, strings are automatically transformed into symbols
  * `cli-args` cli arguments to pass to all tasks to tests
  * `app-registry` is a cust-app defined registry for cust specific tasks"
  [tasks-in-app cli-args app-registry]
  (let [tasks-in-app (mapv symbol tasks-in-app)
        selected-tasks (select-keys (merge build-bb-tasks/registry app-registry)
                                    tasks-in-app)]
    (when-not (= (count tasks-in-app) (count selected-tasks))
      (build-log/warn
        "Some tasks of your application are not defined in any registry - that task can't be tested in la"))
    (build-cli-test/cli-test selected-tasks cli-args)))

(comment
  (run ["clean"] {} {})
  ;
)
