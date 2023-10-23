(ns automaton-build-app.tasks-helper
  "Helpers function to initiate the bb tasks"
  (:require [automaton-build-app.log :as build-log]
            [automaton-build-app.os.commands :as build-cmds]
            [babashka.fs :as fs]
            [babashka.tasks]
            [clojure.pprint :as pp]
            [clojure.tools.cli :refer [parse-opts]]))

(defn- task-name
  "Return the current task name as as string
  Works only in `:enter` or tasks contents (not in `:init` block)"
  []
  (-> (babashka.tasks/current-task)
      :name
      str))

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
  {"clean" [["-w" "--will-be-an EXAMPLE" "Is an example"]],
   "push" [["-m" "--message COMMIT-MESSAGE" "Mandatory: Commit message"]
           ["-t" "--tag-message TAG-MESSAGE" "Tag message"]],
   "gha" [["-f" "--force" "Force execution on local machine"]]})

(defn enter-tasks
  "To be run during the enter of tasks
  Params:
  * `task-specific-cli-opts` app specific cli opts, a map associating a task name as a string to the cli options, as understood by tools.cli"
  [task-specific-cli-opts]
  (let [cli-opts (->> (assemble-opts common-cli-opts
                                     (merge build-app-task-specific-cli-opts
                                            task-specific-cli-opts)
                                     (task-name))
                      (parse-opts *command-line-args*))]
    (build-log/set-min-level! (get-in cli-opts [:options :log]))
    (build-log/set-details? (get-in cli-opts [:options :details]))
    (build-log/trace "Options data")
    (build-log/trace-data cli-opts)
    (assoc cli-opts :usage-msg (format "Usage `bb %s`" (task-name)))))

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
  [body-fn opts]
  (build-log/trace-format "Run %s task on bb" (task-name))
  (let [ns (-> (symbol body-fn)
               namespace
               symbol)]
    (require ns)
    ((resolve body-fn)
      {:command-line-args *command-line-args*, :cli-opts opts})))

(defn- run-clj
  "Run the `body-fn` on the current full clojure environment"
  [body-fn opts]
  (build-log/trace-format "Run %s task on clj" (task-name))
  (build-cmds/execute-and-trace ["clojure" "-X:build" (qualified-name body-fn)
                                 :command-line-args (or *command-line-args* [])
                                 :cli-opts opts :min-level
                                 (build-log/min-level-kw) {}]))

(defn- dispatch
  "Execute the body-fn directy in currently running bb env
  * `body` body to execute
  * `executing-pf` could be :bb or :clj, the task will be executed on one or the other
  * `cli-opts` parsed command line arguments"
  [body-fn executing-pf cli-opts]
  (cond (get-in cli-opts [:options :help]) (println (:summary cli-opts))
        (= :clj executing-pf) (run-clj body-fn cli-opts)
        :else (run-bb body-fn cli-opts)))

(defn execute-task
  "Run the function and manage
  Params:
  * `cli-opts`
  * `body` body to execute
  * `executing-pf` (Optional, default = :bb) could be :bb or :clj, the task will be executed on one or the other"
  [cli-opts body-fn & executing-pf]
  (try (build-log/info-format "Run %s task" (task-name))
       (dispatch body-fn (first executing-pf) cli-opts)
       (catch Exception e
         (println (format "Error during execution of `%s`, %s`"
                          (task-name)
                          (pr-str (ex-message e))))
         (if (cicd?)
           (println e)
           (let [file (fs/create-temp-file {:suffix ".edn"})]
             (println (format "See details in `%s`"
                              (.toString (.toAbsolutePath file))))
             (spit (fs/file file) (prn-str e))
             ""))
         (System/exit 1))))
