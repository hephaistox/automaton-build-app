(ns automaton-build-app.tasks.launcher.pf-dispatcher
  (:require [automaton-build-app.tasks.launcher.launch-on-same-env :as build-launch-on-same-env]
            [automaton-build-app.tasks.launcher.launch-on-clj-env :as build-launch-on-clj-env]
            [automaton-build-app.log :as build-log]))

(defn dispatch
  "Execute the task-fn directly in the currently running bb env or the clj env"
  [{:keys [pf task-name]
    :or {pf :bb}
    :as task-map}
   {:keys [cli-args]
    :as app-data} task-cli-opts]
  (build-log/info-format "Run `%s` task on pf `%s`" task-name pf)
  (cond (= :clj pf) (build-launch-on-clj-env/switch-to-clj task-map app-data task-cli-opts cli-args)
        :else (build-launch-on-same-env/same-env task-map app-data)))
