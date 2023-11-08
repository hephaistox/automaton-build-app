(ns automaton-build-app.code-helpers.bb-edn.updater
  "This namespace ensure the `bb.edn` is up to date
  It gather task and app configuration"
  (:require [automaton-build-app.code-helpers.bb-edn.task-updater :as build-bb-tasks]
            [automaton-build-app.code-helpers.bb-edn :as build-bb-edn]
            [automaton-build-app.code-helpers.build-config :as build-build-config]
            [automaton-build-app.code-helpers.deps-edn :as build-deps-edn]))

(defn- update-bb-tasks
  "Update the tasks in the bb.edn file to reflect the app setup
  * If `select-tasks` is set, only that tasks are used except the ones set in `exclude-tasks`
  * Otherwise, all tasks from `automaton-build-app.bb-tasks` are copied, except the ones in `exclude-tasks` parameter
  Params:
  * `app-dir`
  * `task-registry` "
  [app-dir task-registry]
  (let [{:keys [select-tasks exclude-tasks]} (build-build-config/read-param [:bb-tasks] nil)]
    (build-bb-tasks/update-bb-tasks app-dir task-registry select-tasks exclude-tasks)))

(defn- update-bb-deps
  "Update the dependencies in the `bb.edn` file with the alias of `deps.edn`
  Params:
  * `app-dir`"
  [app-dir]
  (let [deps-edn (build-deps-edn/load-deps-edn app-dir)
        bb-deps (get-in deps-edn [:aliases :bb-deps :extra-deps])]
    (build-bb-edn/update-bb-edn app-dir #(assoc % :deps bb-deps))))

(defn is-uptodate
  "Update the bb file stored in `app-dir` with the set of task `tasks`
Params:
  * `app-dir`
  * `task-registry`"
  [app-dir task-registry]
  (and (update-bb-tasks app-dir task-registry) (update-bb-deps app-dir)))
