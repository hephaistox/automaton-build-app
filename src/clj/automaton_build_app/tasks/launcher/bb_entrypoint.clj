(ns automaton-build-app.tasks.launcher.bb-entrypoint
  "Function to be called from the bb tasks"
  (:require [automaton-build-app.app.bb-edn :as build-bb-edn]
            [automaton-build-app.app.build :as build-app-build]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.os.exit-codes :as build-exit-codes]
            [automaton-build-app.repl.portal :as build-portal]
            [automaton-build-app.tasks.launcher.cli-task-agnostic-opts :as build-cli-task-agnostic-opts]
            [automaton-build-app.tasks.launcher.print-exception :as build-print-exception]
            [automaton-build-app.tasks.launcher.task-execute :as build-tasks-execute]
            [clojure.pprint :as pp]))

(defn -main
  "Entry point for the bb task:
  * Init task agnostic features
  * Run the task called task-name
  * Create the app object

  Design decision:
  * `task-name` could be retrieved from the bb function here, but it is preferable to to do it in bb.edn file, so this one can be executed in the repl
  * cli parsing is done twice. so the first time allow to init the global state, like log
  * system exit are retrieved here so it is clearer and make the code more robust, as it is able to manage nil everywhere.
  * `app-dir` is hard coded here, the one of the current app, all following functions are based on this value, so it is possible to reuse them to build composite app
  * `bb.edn` file has been emptied to keep everything in the registry so no extrawork is needed to synchronize both
  * the application is built here, to load file onces, to have a clear log.
  * Non tasks related initialization are done here,
  * All tasks can return an exit-code, if nil value is returned it will be turned into `ok` code, otherwise, the task return will be interpreted as an exit code

  Params:
  * `cli-args` collection of strings as passed to the cli at startup"
  [[task-name & cli-args]]
  (try (prefer-method pp/simple-dispatch clojure.lang.IPersistentMap clojure.lang.IDeref)
       (build-portal/client-connect)
       (if (build-cli-task-agnostic-opts/common-opts! cli-args task-name)
         build-exit-codes/ok
         (let [app-dir ""
               app (build-app-build/build app-dir)]
           (build-bb-edn/update-bb-edn app)
           (if (nil? task-name)
             (do (build-log/fatal "The task name is missing, please use `bb heph-task task` where task is the name of the task to call")
                 build-exit-codes/invalid-argument)
             (let [exit-code (build-tasks-execute/task-execute app task-name cli-args)]
               (if (int? exit-code)
                 exit-code
                 (do (build-log/warn-format "The task is not working properly, it should return an exit code and it returned %s" exit-code)
                     build-exit-codes/cannot-execute))))))
       (catch Exception e (build-print-exception/print-exception task-name e) build-exit-codes/catch-all)))

(def ^:private detailed-log "If on, that switch turned on the detailed traces" false)

(defn call-main "Utilitary function for text below" [& args] (-main (concat (vec args) (if detailed-log ["-l" "trace" "-d"] []))))
(comment
  (call-main "be-repl")
  (call-main "error")
  (call-main "is-cicd") ;; 1
  (call-main "is-cicd" "-f")
  (call-main "start-storage")
  (call-main "bg")
  (call-main "blog")
  (call-main "clean")
  (call-main "clean-hard")
  (call-main "compile-to-jar")
  (call-main "container-clear")
  (call-main "container-list")
  (call-main "docstring")
  (call-main "format-code")
  (call-main "gha-container-publish") ;;returns 128
  (call-main "gha-container-publish" "-t" "0.0.0")
  (call-main "gha-lconnect")
  (call-main "la")
  (call-main "lbe-test")
  (call-main "lfe-test")
  (call-main "lint")
  (call-main "list")
  (call-main "list" "-h")
  (call-main "mermaid")
  (call-main "mermaid-watch")
  (call-main "push-local-dir-to-repo")
  (call-main "reports")
  (call-main "storage-install")
  (call-main "storage-run")
  (call-main "update-deps")
  (call-main "vizualise-deps")
  (call-main "vizualise-ns")
  (call-main "non-existing-fn")
  (call-main "lint" "-g") ;; 128
  (call-main "gha" "-f" "-d")
  ;;
)
