(ns automaton-build-app.app.deps-graph.impl
  (:require [automaton-build-app.app.deps-edn :as build-deps-edn]
            [automaton-build-app.graph :as graph]
            [automaton-build-app.utils.namespace :as build-namespace]))

(defn add-hephaistox-deps
  "Creates a graph dependency of our apps, i.e. a map associating the lib symbol to a map containing:

  Params:
  * `apps` applications"
  [apps]
  (->> apps
       (map (fn [{:keys [deps-edn build-config]
                  :as app}]
              (let [as-lib (get-in build-config
                                   [:publication :as-lib]
                                   (build-namespace/namespaced-keyword "non-lib" (get build-config :app-name)))]
                [as-lib (assoc app :hephaistox-deps (build-deps-edn/hephaistox-deps deps-edn))])))
       (into {})))

(defn nodes-fn
  [graph]
  (->> graph
       keys
       vec))

(defn edges-fn
  [graph]
  (->> graph
       (mapcat (fn [[app-name {:keys [hephaistox-deps]}]] (mapv (fn [dep-lib] [app-name dep-lib]) hephaistox-deps)))
       vec))

(defn src-in-edge [edge] (first edge))

(defn dst-in-edge [edge] (second edge))

(defn remove-nodes [graph nodes-to-remove] (apply dissoc graph (set nodes-to-remove)))

(defn map-app-lib-to-app
  [deps-graph ordered-libs]
  (->> ordered-libs
       (mapv (fn [app-lib] (get deps-graph app-lib)))))

(defn topologically-sort
  "Sort topologically the graph
  Params:
  * `deps-graph`"
  [deps-graph]
  (->> deps-graph
       (graph/topologically-ordered nodes-fn edges-fn dst-in-edge remove-nodes)
       (map-app-lib-to-app deps-graph)))
