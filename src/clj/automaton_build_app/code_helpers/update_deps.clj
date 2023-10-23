(ns automaton-build-app.code-helpers.update-deps
  "Update the dependencies of the project (both clj and cljs compilers)
  Proxy to antq"
  (:require [automaton-build-app.code-helpers.bb-edn :as build-bb-edn]
            [automaton-build-app.code-helpers.deps-edn :as build-deps-edn]))

(defn update-bb-deps
  "Update the dependencies of the bb.edn file with the alias in bb"
  [dir]
  (let [deps-edn (build-deps-edn/load-deps-edn dir)
        bb-deps (get-in deps-edn [:aliases :bb-deps :extra-deps])]
    (build-bb-edn/update-bb-edn dir
                                (fn [bb-edn] (assoc bb-edn :deps bb-deps)))))
