(ns automaton-build-app.tasks.launcher.pf-dispatcher
  (:require [automaton-build-app.tasks.launcher.launch-on-same-env :as build-launch-on-same-env]
            [automaton-build-app.tasks.launcher.launch-on-clj-env :as build-launch-on-clj-env]))

(defn dispatch
  "Execute the task-fn directy in currently running bb env or the clj env
  * `task-map`
    * `task-fn` body function to execute
    * `pf` could be :bb or :clj, the task will be executed on one or the other
  * `app-dir`
  * `cli-opts` parsed command line arguments
  * `body-fn-args` arguments coming directly from the bb.edn, especially used for workflows"
  [{:keys [task-fn pf]
    :as _task-map} app-dir cli-opts body-fn-args]
  (cond (= :clj pf) (build-launch-on-clj-env/switch-to-clj task-fn app-dir cli-opts body-fn-args)
        :else (build-launch-on-same-env/same-env task-fn app-dir cli-opts body-fn-args)))

(comment
  (dispatch {:task-fn 'automaton-build-app.tasks.workflow-composer/composer
             :pf :clj}
            ""
            {:details true
             :log :trace}
            ['clean 'clean])
  ;;
)
