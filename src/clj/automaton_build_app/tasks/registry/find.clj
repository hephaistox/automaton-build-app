(ns automaton-build-app.tasks.registry.find
  "Function to find elemnts in the registry"
  (:require [automaton-build-app.log :as build-log]))

(defn task-map
  "Return the map describing the task called `task-name`, as defined in the registry
  Params:
  * `registry` as build with `build` fn
  * `task-name` string, name of the task to look at"
  [registry task-name]
  (let [task-map (->> (some-> task-name
                              symbol)
                      (get registry))]
    (when-not task-map (build-log/warn-format "The task `%s` is unknown" task-name))
    task-map))

(defn task-selection
  "Transform a list of task names into a list of task maps"
  [task-registry tasks-names]
  (->> tasks-names
       (mapv (fn [task-name]
               (let [v (get (into {} task-registry) (symbol task-name))]
                 (when-not v (build-log/warn-format "Task called `%s` has not been found in the registry" task-name task-registry))
                 v)))
       (filterv some?)))
