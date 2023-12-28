(ns automaton-build-app.app.bb-edn.deps-updater
  "Update the deps of the bb-edn"
  (:require [automaton-build-app.log :as build-log]))

(defn update-bb-deps
  "Copy the `:extra-deps` of the aliases `:bb-deps` into the `bb.edn`
  Params:
  * `app`"
  [app]
  (if-let [bb-deps (get-in app [:deps-edn :aliases :bb-deps :extra-deps])]
    (update app :bb-edn #(assoc % :deps bb-deps))
    (do (build-log/error "Can't continue `bb.edn` update as `:bb-deps` in `deps.edn` is empty") app)))
