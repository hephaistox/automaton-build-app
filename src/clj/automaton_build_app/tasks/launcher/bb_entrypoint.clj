(ns automaton-build-app.tasks.launcher.bb-entrypoint
  "Function to be called from the bb tasks

  Concept:
  * tasks-helper are some functions easy to use from bb.edn that is minimizing the chance of refactoring bb.edn files"
  (:require [automaton-build-app.cicd.server :as build-cicd-server]
            [automaton-build-app.code-helpers.bb-edn.updater :as build-bb-updater]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.os.exit-codes :as build-exit-codes]
            [automaton-build-app.tasks.launcher.cli-opts :as build-tasks-cli-opts]
            [automaton-build-app.tasks.launcher.pf-dispatcher :as build-pf-dispatcher]
            [automaton-build-app.tasks.launcher.print-or-spit :as build-print-or-spit]
            [automaton-build-app.tasks.registry.find :as build-task-registry-find]
            [automaton-build-app.tasks.registry.global :as build-task-registry-global]
            [clojure.pprint :as pp]))

(defn- update-bb-edn
  "Update the bb.edn file with the task registry"
  [app-dir task-registry]
  (when-not (build-bb-updater/is-uptodate app-dir task-registry)
    (build-log/fatal "The `bb.edn` needed an update, please rerun the task now the new version is installed")
    (System/exit build-exit-codes/catch-all)))

(defn- disaptch-to-pf
  "Dispatch the task execution to the right pf"
  [app-dir task-registry task-name body-fn-args]
  (let [{:keys [specific-cli-opts-kws]
         :as task-map}
        (build-task-registry-find/find-one task-registry task-name)]
    (build-log/info-format "Run %s task on pf `%s`" task-name (get task-map :pf :bb))
    (if-let [cli-opts (build-tasks-cli-opts/cli-opts specific-cli-opts-kws)]
      (build-pf-dispatcher/dispatch task-map app-dir cli-opts (first body-fn-args))
      (System/exit build-exit-codes/catch-all))))

(defn execute-task
  "Run the function and manage

  That function is called by the `bb.edn` file.

  Params:
  * `task-name` is the name of the task currently done. get task-name from current task will invalidate this namespace to be used with a classical repl
  * `body-fn` body to execute. The parameter value is not used as it is picked in the registry. For user ease, the functions are left in `bb.edn`
  * `body-fn-args` Arguments of the `body-fn`"
  [task-name _body-fn & body-fn-args]
  (prefer-method pp/simple-dispatch clojure.lang.IPersistentMap clojure.lang.IDeref)
  (let [app-dir ""]
    (try (let [task-registry (build-task-registry-global/build app-dir)]
           (update-bb-edn app-dir task-registry)
           (disaptch-to-pf app-dir task-registry task-name body-fn-args))
         (catch Exception e
           (println (format "Error during execution of `%s`, %s`" task-name (pr-str (or (ex-message e) e))))
           (build-print-or-spit/exception (build-cicd-server/is-cicd?) e)
           (System/exit build-exit-codes/catch-all)))))
