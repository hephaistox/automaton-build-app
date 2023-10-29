(ns automaton-build-app.tasks.test
  "Tests"
  (:require [automaton-build-app.app :as build-app]
            [automaton-build-app.code-helpers.lint :as build-lint]
            [automaton-build-app.code-helpers.bb-edn :as build-bb-edn]
            [automaton-build-app.containers :as build-containers]
            [automaton-build-app.code-helpers.frontend-compiler :as
             build-frontend-compiler]
            [automaton-build-app.containers.github-action :as
             build-github-action]
            [automaton-build-app.la :as build-la]
            [automaton-build-app.os.exit-codes :as build-exit-codes]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.os.commands :as build-cmds]
            [automaton-build-app.os.files :as build-files]
            [automaton-build-app.cicd.cfg-mgt :as build-cfg-mgt]))

(defn- gha-lconnect*
  [tmp-dir repo-url repo-branch app-name container-repo-account tag]
  (when-not (and (build-cfg-mgt/clone-repo-branch tmp-dir repo-url repo-branch)
                 (some-> (build-github-action/make-github-action
                           app-name
                           tmp-dir
                           ""
                           container-repo-account
                           tag)
                         build-containers/connect))
    (build-log/fatal "Error during gha connection")
    (System/exit build-exit-codes/catch-all)))

(defn gha-lconnect
  "Task to locally connect to github action"
  [{:keys [cli-opts], :as _parsed-cli-opts}]
  (build-log/info "Run and connect to github container locally")
  (let [{:keys [app-name publication], :as app-data} (@build-app/build-app-data_
                                                      "")
        tag (get-in cli-opts [:options :tag])
        container-repo-account (get-in app-data [:container-repo :account])
        {:keys [gha-container]} publication
        {:keys [repo-url workflows repo-branch]} gha-container
        tmp-dir (build-files/create-temp-dir "gha_image")]
    (if (or (nil? repo-url) (nil? workflows))
      (do
        (build-log/fatal
          "Parameters are missing  [:publication :gha-cntainer] in `build_config.edn`")
        (System/exit build-exit-codes/catch-all))
      (gha-lconnect* tmp-dir
                     repo-url
                     repo-branch
                     app-name
                     container-repo-account
                     tag))))

(defn ltest
  "Local tests
  All that tests should be runnable on github action
  `rlwrap` is not on the container image, so `clojure` should be used instead of `clj`"
  [{:keys [min-level], :as _parsed-cli-opts}]
  (build-log/set-min-level! min-level)
  (let [{:keys [ltest shadow-cljs], :as app-data} (@build-app/build-app-data_
                                                   "")
        aliases (get ltest :aliases)]
    (when-not (and (build-lint/lint false (build-app/src-dirs app-data))
                   (build-cmds/execute-and-trace ["clojure"
                                                  (apply str "-M" aliases)])
                   (or (not shadow-cljs)
                       (apply build-cmds/execute-and-trace
                         (concat [["npm" "install"]]
                                 (mapv (fn [build] ["npx" "shadow-cljs"
                                                    "compile" (str build)])
                                   (build-frontend-compiler/builds
                                     "../automaton_web"))
                                 [["npx" "karma" "start" "--single-run"]]))))
      (build-log/fatal "Tests have failed")
      (System/exit build-exit-codes/catch-all))))

(defn la
  "Local acceptance"
  [{:keys [min-level], :as parsed-cli-args}]
  (let [task-names-in-bb (build-bb-edn/task-names "")]
    (build-log/set-min-level! min-level)
    (build-log/trace-format "The following tasks are found in `bb.edn`: %s"
                            task-names-in-bb)
    (build-la/run task-names-in-bb
                  (get-in parsed-cli-args [:command-line-args])
                  {})))
