(ns automaton-build-app.tasks.launcher.pf-dispatcher
  (:require [automaton-build-app.tasks.launcher.launch-on-same-env :as build-launch-on-same-env]
            [automaton-build-app.tasks.launcher.launch-on-clj-env :as build-launch-on-clj-env]
            [automaton-build-app.log :as build-log]))

(defn dispatch
  "Execute the task-fn directy in currently running bb env or the clj env"
  [{:keys [task-fn pf task-name]
    :or {pf :bb}} cli-opts app body-fn-args]
  (build-log/info-format "Run `%s` task on pf `%s`" task-name pf)
  (cond (= :clj pf) (build-launch-on-clj-env/switch-to-clj task-fn cli-opts app body-fn-args)
        :else (build-launch-on-same-env/same-env task-fn cli-opts app body-fn-args)))

(comment
  (dispatch {:task-fn 'automaton-build-app.tasks.workflow.composer/composer
             :task-name "double-clean"
             :pf :clj}
            {:app-dir ""}
            {:details true
             :log :trace}
            ['clean 'clean])
  ;;
)
