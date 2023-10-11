(ns automaton-build-app.tasks.test
  "Tests"
  (:require
   [automaton-build-app.apps.app :as build-app]
   [automaton-build-app.code-helpers.build-config :as build-build-conf]
   [automaton-build-app.containers :as build-containers]
   [automaton-build-app.containers.github-action :as build-github-action]
   [automaton-build-app.os.commands :as build-cmds]))

(defn ltest
  "Local tests
  All are made to be executed on github
  `rlwrap` is not on the container image, so clojure should be used instead of `clj`
  Params:
  * `aliases` collection of aliases to use in the tests"
  [& aliases]
  (let [app-data (build-app/build-app-data "")]
    (when (build-cmds/execute-and-trace ["clojure" (apply str "-M"
                                                          aliases)])
      (when (:shadow-cljs app-data)
        (build-cmds/execute-and-trace ["npm" "install"]
                                      ["npx" "shadow-cljs" "compile" "ltest"]
                                      ["npx" "karma" "start" "--single-run"])))))

(defn gha-lconnect
  "Task to locally connect to github action"
  []
  (let [{:keys [app-name] :as  build-config-data} (build-build-conf/read-build-config "")
        container-repo-account (get-in build-config-data
                                       [:container-repo :account])
        ;;TODO Should come from a github repo
        tmp-dir "../../container_images/gha_image"
        container (build-github-action/make-github-action app-name
                                                          tmp-dir
                                                          ""
                                                          container-repo-account)]
    (build-containers/connect container)))
