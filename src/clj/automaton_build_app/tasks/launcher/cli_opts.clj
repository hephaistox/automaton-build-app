(ns automaton-build-app.tasks.launcher.cli-opts
  "Cli options for tasks"
  (:require [automaton-build-app.log :as build-log]
            [clojure.string :as str]
            [clojure.tools.cli :refer [parse-opts]]))

(defn- cli-opts-spec-register
  "Register container all specs of cli options"
  []
  {:common [["-l" "--log LOG-LEVEL" "Log level, one of `trace`, `debug`, `info`, `warning`, `error`, `fatal`" :default :info :parse-fn
             keyword :validate
             [(partial contains? #{:trace :debug :fatal :warning :info :error})
              "Must be one of `trace`, `debug`, `info`, `warning`, `error`, `fatal`"]]
            ["-d" "--details" "Show details and don't ellipsis the log"] ["-h" "--help" "Displays this help message"]]
   :force [["-f" "--force" "Force execution on local machine"]]
   :tag [["-t" "--tag TAG-MESSAGE" "Tag for the publication"]]
   :commit [["-m" "--message COMMIT-MESSAGE" "Mandatory: Commit message"]]})

(defn print-error-message
  "Print the error message, tell what's expected"
  ([cli-opts msg]
   (println (format (cond (str/blank? msg) "The cli arguments are invalid"
                          :else msg)))
   (some-> (:errors cli-opts)
           println)
   (println (:summary cli-opts)))
  ([cli-opts] (print-error-message cli-opts "")))

(defn print-help-message
  "Print the help message
  Params:
  * `task-name`
  * `cli-opts`"
  [task-name cli-opts]
  (println (format "`bb %s` usage" task-name))
  (println (:summary cli-opts)))

(defn mandatory-option
  "Print an error message if the option is not found
  Returns the value at adress `ks`
  Design decision:
  * It is a choice not to use log just to be sure it is printed on the terminal, without any ellipsis and prefix
  Params:
  * `cli-opts`
  * `ks` sequence of keyword to tell the path of the searched value"
  [cli-opts ks]
  (let [ks (vec (concat [:options] ks))]
    (if-let [v (get-in cli-opts ks)]
      v
      (print-error-message cli-opts (format "The argument %s is mandatory" ks)))))

(defn do-common-opts
  "Do what common cli opts should do
  Params:
  * `cli-opts` options returned by the cli
  Returns the help message if it is required by the user
  Returns nil otherwise"
  [cli-opts]
  (build-log/set-min-level! (get-in cli-opts [:options :log]))
  (build-log/set-details? (get-in cli-opts [:options :details]))
  (build-log/trace-data cli-opts "do common opts")
  (when (get-in cli-opts [:options :help]) (:summary cli-opts)))

(defn- cli-opts-spec
  "For given `cli-opts-spec-kw`s, creates the cli options specifications
  Params:
  * `cli-opts-spec-kws` collection of name of the options to build the cli opt spec for"
  [cli-opts-spec-kws]
  (let [cli-opts-spec (cli-opts-spec-register)
        common-specs (get cli-opts-spec :common)
        specific-specs (mapcat #(get cli-opts-spec %) cli-opts-spec-kws)]
    (-> (concat common-specs specific-specs)
        vec)))

(defn cli-opts
  "Get the cli-opts from the spec
  Params:
  * `cli-opts-spec-kw` name of the option to build the cli opt spec for
  * `cli-args` arguments of the cli as in `*command-line-args*`"
  [cli-opts-spec-kw cli-args]
  (let [cli-opts-specs (cli-opts-spec cli-opts-spec-kw)
        cli-opts (parse-opts cli-args cli-opts-specs)]
    (when-not (do-common-opts cli-opts) cli-opts)))

(comment
  (cli-opts-spec [:force :tag])
  ;
)
