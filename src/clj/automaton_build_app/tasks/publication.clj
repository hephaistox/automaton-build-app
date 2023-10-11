(ns automaton-build-app.tasks.publication
  "For one application publication to its configuration management
  The source is the local files"
  (:require
   [automaton-build-app.apps.app :as build-app]
   [automaton-build-app.cicd.cfg-mgt :as build-cfg-mgt]))

(defn push-from-local
  "Push the current repository.
  Use with care, the source truth is monorepo commits"
  [commit-msg]
  (let [app-data (build-app/build-app-data "")]
    (build-cfg-mgt/push-local-dir-to-repo "."
                                          (get-in app-data [:publication :repo :address])
                                          (get-in app-data [:publication :repo :branch])
                                          (build-cfg-mgt/current-branch ".")
                                          commit-msg)))
