(ns automaton-build-app.os.commands
  "Execute a process
  Is a proxy for `babashka.process`"
  (:require [automaton-build-app.log :as build-log]
            [babashka.process :as p]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(def default-opts {:in :inherit, :out :inherit, :shutdown p/destroy-tree})

(defn- log-a-stream
  "Connect to output and error stream and log them
   Params:
  * `logger-fn` function to use to log the line
  * `proc` the processus linked to that proc
  * `stream` the stream to listen at"
  [logger-fn proc stream]
  (with-open [rdr (io/reader stream)]
    (binding [*in* rdr]
      (loop []
        (when-let [line (read-line)] (logger-fn line))
        (when-not (string? stream)
          (when (.isAlive (:proc proc)) (Thread/sleep 10))
          (when (or (.ready rdr) (.isAlive (:proc proc))) (recur)))))))

(defn- log-during-execution
  "Map a processus streams to output and error streams
  Params:
  * `proc` the process to listen at"
  [proc]
  (future (log-a-stream (fn [& args] (build-log/trace (doall args)))
                        proc
                        (:out proc)))
  (log-a-stream (fn [& args] (build-log/error (doall args))) proc (:err proc)))

(defn- create-process
  "Creates process and execute it according to the params
  Params:
  * `command` to execute. The last element in opts is an optional map of options
  * `trace?` if true, the output and error streams are linked to log
  * `string?` if true, the output and error streams are returned as a string"
  [command trace? string?]
  (try
    (let [last-command-elt (last command)
          [command opts] (if (map? last-command-elt)
                           [(vec (butlast command))
                            (merge default-opts last-command-elt)]
                           [command default-opts])
          updated-opts (-> opts
                           (dissoc :background?)
                           (merge (when string? {:out :string, :err :string}))
                           (update :dir #(if (str/blank? %) "." %)))
          _ (build-log/trace-format "Execute `%s` with options = `%s`"
                                    (str/join " " command)
                                    (pr-str updated-opts))
          process (apply p/process updated-opts command)]
      (when trace? (log-during-execution process))
      (cond (:background? opts) true
            :else (let [{:keys [exit out err]} @process] [exit (str out err)])))
    (catch Exception e
      (build-log/error-exception e)
      [-1 (str "Unexpected error during execution of this command" command)])))

(defn execute-with-exit-code
  "Execute the commands, returns a vector with, for each command, a pair of exit code and message
  Params:
  * `commands` is a sequence of vectors
  Each vector is a command to execute
  If the last element is a map, it is used as an option map
  That option map could be:
  * `:background?` if true the process is done in the background"
  [& commands]
  (vec (for [command commands] (create-process command false true))))

(defn- execute*
  "Private function to factorize code executions"
  [commands trace? string?]
  (vec (for [command commands] (create-process command trace? string?))))

(defn execute-and-trace
  "Execute the commands and trace their result in the log
  Note that if the last parameter is a map, it is recognized as options. If you need to pass a map as a param (for a clojure -X for instance), just add the optional map to the caller

  Params:
  * `commands` is a sequence of vectors
  Each vector is a command to execute
  If the last element is a map, it is used as an option map
  That option map could be:
  * `:background?` if true the process is done in the background"
  [& commands]
  (->> (execute* commands true false)
       (every? (comp zero? first))))

(defn execute-and-trace-return-exit-codes
  "Execute the commands and trace their result, return the exit code and messages
  Note that if the last parameter is a map, it is recognized as options. If you need to pass a map as a param (for a clojure -X for instance), just add the optional map to the caller

  Params:
  * `commands` is a sequence of vectors
  Each vector is a command to execute
  If the last element is a map, it is used as an option map
  That option map could be:
  * `:background?` if true the process is done in the background"
  [& commands]
  (execute* commands true false))

(defn execute-get-string
  "Execute the commands and returns a vector of string, one for each command, appending output and error stream as string, in that order
  Params:
  * `commands`"
  [& commands]
  (->> (execute* commands false true)
       (mapv second)))

(defn first-cmd-failing
  "If process is run with exit code,
  Returns the position of the first failing command"
  [command-res-with-exit-code]
  (->> command-res-with-exit-code
       (map-indexed (fn [item idx] [idx item]))
       (filter (comp pos? ffirst))
       first
       ((juxt second (comp second first)))))

(defn expand-cmd
  "Expand the command to a string
  Remove the trailing map of options if exist
  Params:
  * `cmd` a command, as a list of strings optionally ended by the option map"
  [cmd]
  (if (map? (last cmd)) (str/join " " (butlast cmd)) (str/join " " cmd)))
