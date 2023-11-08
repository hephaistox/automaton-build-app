(ns automaton-build-app.tasks.launcher.cli-opts
  "Cli options for tasks"
  (:require [automaton-build-app.log :as build-log]
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

(defn do-common-opts
  "Do what common cli opts should do
  Params:
  * `cli-opts` options returned by the cli
  Returns the error message if the options parsing is finding an error like non existing switch
  Returns nil otherwise"
  [cli-opts]
  (build-log/set-min-level! (get-in cli-opts [:options :log]))
  (build-log/set-details? (get-in cli-opts [:options :details]))
  (cond (get-in cli-opts [:options :help]) (:summary cli-opts)
        (:errors cli-opts) (do (build-log/fatal-format "The cli arguments are invalid\n%s" (:summary cli-opts)) nil)))

(defn- cli-opts-spec
  "For given `cli-opts-spec-kw`s, creates the cli options specifications
  Params:
  * `cli-opts-spec-kws` name of the options to build the cli opt spec for"
  [cli-opts-spec-kws]
  (let [cli-opts-spec (cli-opts-spec-register)
        common-specs (get cli-opts-spec :common)
        specific-specs (mapcat #(get cli-opts-spec %) cli-opts-spec-kws)]
    (-> (concat common-specs specific-specs)
        vec)))

(defn cli-opts
  "Get the cli-opts from the spec
  Params:
  * `cli-opts-spec-kw` name of the option to build the cli opt spec for"
  [cli-opts-spec-kw]
  (let [cli-opts-specs (cli-opts-spec cli-opts-spec-kw)
        cli-opts (parse-opts *command-line-args* cli-opts-specs)]
    (if-let [non-compliance-message (do-common-opts cli-opts)]
      (do (println (format "Invalid option\n%s" non-compliance-message)) nil)
      cli-opts)))

(comment
  (cli-opts-spec [:force :tag])
  ;
)
