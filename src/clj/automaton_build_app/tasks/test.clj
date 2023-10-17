(ns automaton-build-app.tasks.test
  "Tests"
  (:require
   [automaton-build-app.app :as build-app]
   [automaton-build-app.code-helpers.lint :as build-lint]
   [automaton-build-app.containers :as build-containers]
   [automaton-build-app.containers.github-action :as build-github-action]
   [automaton-build-app.log :as build-log]
   [automaton-build-app.os.commands :as build-cmds]))

(defn ltest
  "Local tests
  All that tests should be runnable on github action
  `rlwrap` is not on the container image, so clojure should be used instead of `clj`"
  [& _opts]
  (let [{:keys [ltest shadow-cljs]
         :as app-data} (build-app/build-app-data "")
        aliases (get ltest
                     :aliases)]
    (when (build-cmds/execute-and-trace ["clojure" (apply str "-M"
                                                          aliases)])
      (build-lint/lint true
                       (build-app/src-dirs app-data))
      (when (:shadow-cljs shadow-cljs)
        (build-cmds/execute-and-trace ["npm" "install"]
                                      ["npx" "shadow-cljs" "compile" "ltest"]
                                      ["npx" "karma" "start" "--single-run"])))))

(defn gha-lconnect
  "Task to locally connect to github action"
  [& _opts]
  (let [{:keys [app-name]
         :as app-data} (build-app/build-app-data "")
        container-repo-account (get-in app-data
                                       [:container-repo :account])
        ;;TODO Should come from a github repo
        tmp-dir "../../container_images/gha_image"
        container (build-github-action/make-github-action app-name
                                                          tmp-dir
                                                          ""
                                                          container-repo-account)]
    (when container
      (build-containers/connect container))))

(defn gha
  [& _opts]
  (if (or (System/getenv "CI")
          (= "-f"
             (first *command-line-args*)))
    (ltest)
    (build-log/error "This task if for CI, use `bb ltest` instead (or -f to force it to test it locally)")))
