(ns automaton-build-app.tasks-helper
  "Helpers function to initiate the bb tasks"
  (:require [automaton-build-app.log :as build-log]
            [automaton-build-app.os.commands :as build-cmds]
            [automaton-build-app.code-helpers.update-deps :as build-update-deps]
            [automaton-build-app.os.exit-codes :as build-exit-codes]
            [babashka.fs :as fs]
            [clojure.pprint :as pp]
            [clojure.tools.cli :refer [parse-opts]]))

(defn- assemble-opts
  "Assemble task specific options and common cli options"
  [common-cli-opts task-specific-cli-opts *task-name]
  (-> (get task-specific-cli-opts *task-name)
      (concat common-cli-opts)))

(defn init-tasks
  "To be run during the init of tasks"
  []
  (prefer-method pp/simple-dispatch
                 clojure.lang.IPersistentMap
                 clojure.lang.IDeref))

(def common-cli-opts
  [["-l" "--log LOG-LEVEL"
    "Log level, one of `trace`, `debug`, `info`, `warning`, `error`, `fatal`"
    :default :info :parse-fn keyword :validate
    [(partial contains? #{:trace :debug :fatal :warning :info :error})
     "Must be one of `trace`, `debug`, `info`, `warning`, `error`, `fatal`"]]
   ["-d" "--details" "Details during logs"] ["-h" "--help" "Help"]])

(def build-app-task-specific-cli-opts
  {"push" [["-m" "--message COMMIT-MESSAGE" "Mandatory: Commit message"]
           ["-t" "--tag-message TAG-MESSAGE" "Tag message"]],
   "gha-container-publish" [["-t" "--tag TAG" "Tag for the publication"]],
   "gha" [["-f" "--force" "Force execution on local machine"]]})

(defn enter-tasks
  "To be run during the enter of tasks
  Params:
  * `task-specific-cli-opts` app specific cli opts, a map associating a task name as a string to the cli options, as understood by tools.cli"
  [task-name task-specific-cli-opts]
  (let [cli-opts (->> (assemble-opts common-cli-opts
                                     (merge build-app-task-specific-cli-opts
                                            task-specific-cli-opts)
                                     task-name)
                      (parse-opts *command-line-args*))]
    (build-log/set-min-level! (get-in cli-opts [:options :log]))
    (build-log/set-details? (get-in cli-opts [:options :details]))
    (build-log/trace "Options data")
    (build-log/trace-data cli-opts)
    (assoc cli-opts :usage-msg (format "Usage `bb %s`" task-name))))

(defn- cicd?
  "Is the current execution is in the content of a CICD runner, like github runner"
  []
  (System/getenv "CI"))

(defn- qualified-name
  "Return the qualified name of a function"
  [s]
  (apply str (interpose "/" ((juxt namespace name) (symbol s)))))

(defn- run-bb
  "Run the `body-fn` on the current bb environment"
  [task-name body-fn opts]
  (build-log/trace-format "Run %s task on bb" task-name)
  (let [ns (-> (symbol body-fn)
               namespace
               symbol)
        _ (require ns)
        resolved-body-fn (resolve body-fn)]
    (if (nil? resolved-body-fn)
      (build-log/fatal-format "Unknown task `%s` with fn `%s`"
                              task-name
                              body-fn)
      (resolved-body-fn {:command-line-args *command-line-args*,
                         :min-level (build-log/min-level-kw),
                         :cli-opts opts}))))

(defn- run-clj
  "Run the `body-fn` on the current full clojure environment"
  [task-name body-fn opts]
  (build-log/trace-format "Run %s task on clj" task-name)
  (when-not (build-cmds/execute-and-trace
              ["clojure" "-X:build:bb-deps" (qualified-name body-fn)
               :command-line-args (or *command-line-args* []) :cli-opts opts
               :min-level (build-log/min-level-kw) {}])
    (build-log/trace
      "The clj command has failed, so the exit code is passed to the bb")
    (System/exit build-exit-codes/catch-all)))

(defn- dispatch
  "Execute the body-fn directy in currently running bb env
  * `body` body to execute
  * `executing-pf` could be :bb or :clj, the task will be executed on one or the other
  * `cli-opts` parsed command line arguments"
  [task-name body-fn executing-pf cli-opts]
  (cond (get-in cli-opts [:options :help]) (println (:summary cli-opts))
        (= :clj executing-pf) (run-clj task-name body-fn cli-opts)
        :else (run-bb task-name body-fn cli-opts)))

(defn execute-task
  "Run the function and manage
  Params:
  * `cli-opts`
  * `body` body to execute
  * `executing-pf` (Optional, default = :bb) could be :bb or :clj, the task will be executed on one or the other"
  ([task-name cli-opts body-fn {:keys [executing-pf], :or {executing-pf :bb}}]
   (build-update-deps/update-bb-deps "")
   (try (build-log/info-format "Run %s task" task-name)
        (dispatch task-name body-fn executing-pf cli-opts)
        (catch Exception e
          (println (format "Error during execution of `%s`, %s`"
                           task-name
                           (pr-str (or (ex-message e) e))))
          (if (cicd?)
            (println e)
            (let [file (fs/create-temp-file {:suffix ".edn"})]
              (println (format "See details in `%s`"
                               (.toString (.toAbsolutePath file))))
              (spit (fs/file file) (prn-str e))
              ""))
          (System/exit build-exit-codes/catch-all))))
  ([task-name cli-opts body-fn] (execute-task task-name cli-opts body-fn {})))
