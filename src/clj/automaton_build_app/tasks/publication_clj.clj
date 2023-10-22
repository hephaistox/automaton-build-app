(ns automaton-build-app.tasks.publication-clj
  "For one application publication to its configuration management
  The source is the local files"
  (:require [automaton-build-app.app :as build-app]
            [automaton-build-app.code-helpers.formatter :as
             build-code-formatter]
            [automaton-build-app.file-repo.clj-code :as build-clj-code]
            [automaton-build-app.cicd.cfg-mgt :as build-cfg-mgt]
            [automaton-build-app.log :as build-log]))

(defn push-from-local
  "Push the current repository.
  Use with care, the source truth is monorepo commits
  Params:
  * `commit-msg` commit message
  * `tag-msg` tag message"
  [{:keys [min-level], :as opts}]
  (build-log/set-min-level! min-level)
  (let [command-line-args (:command-line-args opts)
        [commit-msg] command-line-args]
    (if (some nil? [commit-msg])
      (println "Usage: bb push [commit message] [tag message]")
      (do (build-log/debug-format "Push local `%s` " commit-msg)
          (let [app-data (@build-app/build-app-data_ "")
                clj-repo (-> app-data
                             build-app/src-dirs
                             build-clj-code/make-clj-repo-from-dirs)
                repo (get-in app-data [:publication :repo])
                {:keys [address branch]} repo]
            (build-code-formatter/code-files-formatted clj-repo)
            (build-cfg-mgt/push-local-dir-to-repo
              "."
              address
              branch
              commit-msg
              commit-msg
              (get-in app-data [:publication :major-version])))))))
