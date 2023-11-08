(ns automaton-build-app.tasks.registry.global
  "Build, validate and access the global registry for the automaton-build-app"
  (:require [automaton-build-app.log :as build-log]
            [automaton-build-app.schema :as build-schema]
            [automaton-build-app.tasks.registry.common :as build-tasks-common]
            [automaton-build-app.tasks.registry.specific-task :as build-specific-task-registry]))

(def ^:private schema
  [:map-of :symbol
   [:map {:closed true} [:doc :string] [:specific-cli-opts-kws {:optional true} [:vector :keyword]]
    [:specific-cli-opts {:optional true} [:vector [:vector :string]]] [:pf {:optional true} :keyword] [:bb-edn-args {:optional true} :any]
    [:task-fn [:or fn? :symbol]] [:group {:optional true} :keyword] [:step {:optional true} pos-int?]
    [:wk-tasks {:optional true} [:vector :symbol]]
    [:la-test {:optional true}
     [:map {:closed true} [:skip? {:optional true} :boolean] [:process-opts {:optional true} :map] [:cmd [:vector :string]]]]]])

(defn build
  "Build the global registry for that app.
  It gather the data from the cust-app and common registry"
  [app-dir]
  (let [tasks-registry-map (->> (merge (build-tasks-common/tasks) (build-specific-task-registry/read-specific-tasks app-dir))
                                (into (sorted-map)))]
    (when-not (build-schema/valid? schema tasks-registry-map) (build-log/error "The bb task registry does not comply the schema"))
    tasks-registry-map))