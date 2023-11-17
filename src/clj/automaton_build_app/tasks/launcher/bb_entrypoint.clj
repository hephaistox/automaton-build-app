(ns automaton-build-app.tasks.launcher.bb-entrypoint
  "Function to be called from the bb tasks"
  (:require ;; [automaton-build-app.code-helpers.bb-edn.updater :as build-bb-updater]
    ;; [automaton-build-app.log :as build-log]
    [automaton-build-app.cicd.server :as build-cicd-server] ;; [automaton-build-app.code-helpers.bb-edn.updater :as build-bb-updater]
    [automaton-build-app.app :as build-app]
    [automaton-build-app.os.exit-codes :as build-exit-codes]
    [automaton-build-app.tasks.launcher.cli-opts :as build-tasks-cli-opts]
    [automaton-build-app.tasks.launcher.pf-dispatcher :as build-pf-dispatcher]
    [automaton-build-app.tasks.launcher.print-or-spit :as build-print-or-spit]
    [automaton-build-app.tasks.registry.find :as build-task-registry-find]
    [automaton-build-app.tasks.registry.global :as build-task-registry-global]))

(defn execute-task*
  [task-name cli-args app-dir bb-edn-arg]
  (let [task-registry (build-task-registry-global/build app-dir)]
    (when-let [task-map (build-task-registry-find/task-map task-registry task-name)]
      (let [{:keys [specific-cli-opts-kws]} task-map
            cli-opts (build-tasks-cli-opts/cli-opts specific-cli-opts-kws cli-args)
            app (->> (build-app/build app-dir)
                     (build-app/update-app task-registry)
                     build-app/save)]
        (cond (nil? cli-opts) (build-tasks-cli-opts/print-help-message task-name cli-opts)
              (:error cli-opts) (do (build-tasks-cli-opts/print-error-message cli-opts "The options are not compatible")
                                    (System/exit build-exit-codes/catch-all))
              :else (build-pf-dispatcher/dispatch task-map cli-opts app bb-edn-arg))))))

(defn execute-task
  "Run the function and manage

  * Init the app
  * Creates the task registry
  That function is called by the `bb.edn` file.

  Design decision:
  * `task-name` could be retrieved from the bb function here, but it is preferable to to do it in bb.edn file, so this one can be executed in the repl
  * cli parsing is done twice. so the first time allow to init the global state, like log
  * system exit are retrieved here so it is clearer and make the code more robust, as it is able to manage nil everywhere
  * `app-dir` is hard coded here, the one of the current app, all following functions are based on this value, so it is possible to reuse them to build composite app
  * the application is built here, to load file onces, to have a clear log, to better

  Params:
  * `task-name` is the name of the task currently done.
  * `body-fn` body to execute. The parameter value is not used as it is picked in the registry. For user ease, the functions are left in `bb.edn`
  * `bb-edn-args` Arguments of the `body-fn`"
  [task-name _body-fn & bb-edn-args]
  (try (build-app/init!)
       (let [app-dir ""] (execute-task* task-name *command-line-args* app-dir (first bb-edn-args)))
       (catch Exception e
         (println (format "Error during execution of `%s`, %s`" task-name (pr-str (or (ex-message e) e))))
         (build-print-or-spit/exception (build-cicd-server/is-cicd?) e)
         (System/exit build-exit-codes/catch-all))))
