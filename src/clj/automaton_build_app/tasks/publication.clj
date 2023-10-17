(ns automaton-build-app.tasks.publication
  "For one application publication to its configuration management
  The source is the local files"
  (:require
   [automaton-build-app.app :as build-app]
   [automaton-build-app.cicd.cfg-mgt :as build-cfg-mgt]
   [automaton-build-app.log :as build-log]))

(defn push-from-local
  "Push the current repository.
  Use with care, the source truth is monorepo commits
  Params:
  * `commit-msg` commit message
  * `tag-msg` tag message"
  [opts]
  ;;TODO Add formatter
  (let [command-line-args (:command-line-args opts)
        [commit-msg tag-msg] command-line-args]
    (if (some nil?
              [commit-msg tag-msg])
      (println "Usage: bb push [commit message] [tag message]")
      (do
        (build-log/debug-format "Push local %s - %s"
                                commit-msg tag-msg)
        (let [app-data (build-app/build-app-data "")
              repo (get-in app-data
                           [:publication :repo])
              {:keys [address branch]} repo]
          (build-cfg-mgt/push-local-dir-to-repo "."
                                                address
                                                branch
                                                commit-msg
                                                tag-msg))))))
