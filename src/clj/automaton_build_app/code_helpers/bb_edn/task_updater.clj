(ns automaton-build-app.code-helpers.bb-edn.task-updater
  "Update the tasks in the bb edn file

  Note that:
  * It is cust-app responsability to tell what tasks should be presented to the user or not
  * cust-app can choose to use all these tasks, and remove a selection
  * or cust-app can select only some all-tasks
  * Whatever that choice, all unknown tasks from this list are left unchanged by `automaton-build-app`, so cust-app can have its own set of specific tasks
  * The source of truth of tasks are here, what's in `bb.edn` is just a copy necessary for bb startup"
  (:require [automaton-build-app.code-helpers.bb-edn :as bb-edn]))

(defn- registry-item-to-bb-task
  "Create a bb task from a bb registry item
  Tasks are referencing the real function to ease developper workflow: so searching the entry point can be started in the bb.edn file and can \"jump to definition\"
  Params:
  * `registry-bb-task`"
  [{:keys [doc task-fn wk-tasks]
    :as _registry-bb-task}]
  {:doc doc
   :task (->> (concat ['execute-build-app-task (list 'quote task-fn)] (when wk-tasks (mapv #(list 'quote %) wk-tasks)))
              (apply list))})

(defn- registry-items-to-bb-task
  "Add tasks from `registry-tasks` to `bb-edn-tasks`
  Params:
  * `registry-tasks` List of tasks to add, coming from the registry, and ready to be inserted
  * `bb-edn-tasks` content of the `bb.edn` file"
  [registry-tasks bb-edn-tasks]
  (->> registry-tasks
       (map (fn [[k v]] [k (registry-item-to-bb-task v)]))
       (into {})
       (merge bb-edn-tasks)))

(defn- remove-bb-tasks
  [registry-tasks bb-edn-tasks]
  (let [bb-edn-tasks (into {} bb-edn-tasks)]
    (->> registry-tasks
         (map symbol)
         set
         (apply dissoc bb-edn-tasks))))

(defn- update-bb-tasks*
  "Update the bb tasks in the `bb.edn` file
  Params:
  * `registry-bb-tasks`
  * `exclude-tasks`
  * `bb-content`"
  [registry-bb-tasks exclude-tasks bb-content]
  (-> bb-content
      (update :tasks (partial registry-items-to-bb-task registry-bb-tasks))
      (update :tasks (partial remove-bb-tasks exclude-tasks))))

(defn update-bb-tasks
  "Update the bb tasks in the `bb.edn` file
  Params:
  * `app-dir` the directory where to look at the bb.edn file
  * `task-registry` registry of tasks to build
  * `bb-tasks-name` list of tasks to be selected (could be string but will be changed into symbols)
  * `exclude-tasks` set of tasks to exclude"
  [app-dir task-registry select-tasks exclude-tasks]
  (let [select-tasks (->> select-tasks
                          (mapv symbol)
                          set)
        exclude-tasks (->> exclude-tasks
                           (map symbol)
                           (remove select-tasks)
                           set)]
    (when (bb-edn/update-bb-edn app-dir (partial update-bb-tasks* task-registry exclude-tasks)) true)))
