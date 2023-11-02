(ns automaton-build-app.tasks.update-deps
  (:require [automaton-build-app.code-helpers.update-deps-clj :as build-update-deps-clj]
            [automaton-build-app.log :as build-log]))

(defn update-deps
  "Update the dependencies of the project"
  [{:keys [min-level details]
    :as _parsed-cli-opts}]
  (build-log/set-min-level! min-level)
  (build-log/set-details? details)
  (build-update-deps-clj/do-update ""))
