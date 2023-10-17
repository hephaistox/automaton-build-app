(ns automaton-build-app.os.commands
  "Execute a process
  Is a proxy for babashka.process"
  (:require
   [automaton-build-app.log :as build-log]
   [babashka.process :as p]
   [clojure.java.io :as io]
   [clojure.string :as str]))

(def default-opts
  {:in :inherit
   :out :inherit})

(defn- log-a-stream
  [logger-fn proc stream]
  (with-open [rdr (io/reader stream)]
    (binding [*in* rdr]
      (loop []
        (when-let [line (read-line)]
          (logger-fn line))
        (when-not (string? stream)
          (when (or (.ready rdr)
                    (.isAlive (:proc proc)))
            (Thread/sleep 10)
            (recur)))))))

(defn log-during-execution
  [proc]
  (future (log-a-stream (fn [& args]
                          (build-log/trace args))
                        proc
                        (:out proc)))
  (log-a-stream (fn [& args]
                  (build-log/error (doall args)))
                proc
                (:err proc)))

(defn execute
  "Execute the commands
  Params:
  * `commands` is a sequence of vectors
  Each vector is a command to execute
  If the last element is a map, it is used as an option map
  That option map could be:
  * `:background?` if true the process is done in the background"
  [& commands]
  (doall
   (for [command commands]
     (try
       (let [last-command-elt (last command)
             [command opts] (if (map? last-command-elt)
                              [(vec (butlast command)) (merge default-opts
                                                              last-command-elt)]
                              [command default-opts])
             _ (build-log/trace-format "Execute `%s`, with options = `%s`" (str/join " " command) (pr-str opts))
             process (apply p/process
                            (dissoc opts
                                    :background?)
                            command)]
         (when-not (:background? opts)
           (let [{:keys [exit out err]} @process]
             [exit (str out err)])))
       (catch Exception e
         (build-log/error-exception e)
         [-1 (str "Unexpected error during execution of this command" command)])))))

(defn- execute*
  "Private function to factorize code executions"
  [commands trace? string?]
  (for [command commands]
    (try
      (let [last-command-elt (last command)
            [command opts] (if (map? last-command-elt)
                             [(vec (butlast command)) (merge default-opts
                                                             last-command-elt)]
                             [command default-opts])
            updated-opts (-> opts
                             (dissoc :background?)
                             (merge (when string?
                                      {:out :string
                                       :err :string}))
                             (update :dir #(if (str/blank? %)
                                             "."
                                             %)))
            _ (build-log/trace-format "Execute `%s` with options = `%s`"(str/join " " command) (pr-str updated-opts))
            process (apply p/process updated-opts
                           command)]
        (when trace?
          (log-during-execution process))
        (cond
          string? (str (:out @process)
                       (:err @process))
          (:background? opts) (do @process true)
          :else true))
      (catch Exception e
        (build-log/error-exception e)
        false))))

(defn execute-and-trace
  "Execute the commands and trace their result
  Params:
  * `commands` is a sequence of vectors
  Each vector is a command to execute
  If the last element is a map, it is used as an option map
  That option map could be:
  * `:background?` if true the process is done in the background"
  [& commands]
  (->> (execute* commands true false)
       (every? identity)))

(defn execute-silently
  "Execute the commands and trace their result
  Params:
  * `commands` is a sequence of vectors
  Each vector is a command to execute
  If the last element is a map, it is used as an option map
  That option map could be:
  * `:background?` if true the process is done in the background"
  [& commands]
  (->> (execute* commands false true)
       (every? identity)))

(defn execute-get-string
  [& commands]
  (execute* commands false true))

(defn first-cmd-failing
  "Return the position of the first failing command"
  [command-res]
  (->> command-res
       (map first)
       (map-indexed (fn [item idx]
                      [idx item]))
       (filter (comp pos? first))
       first
       second))
