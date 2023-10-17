(ns automaton-build-app.tasks.code-helpers-clj
  "Separate namespace from code-helpers so the transitive dependencies of this namespace don't mess upt bb "
  (:require
   [automaton-build-app.code-helpers.update-deps :as build-update-deps]))

(defn update-deps
  "Update the dependencies of the project"
  [& _opts]
  (build-update-deps/do-update))
