(ns automaton-build-app.os.commands
  "Execute a process
  Is a proxy for babashka.process"
  (:require
   [automaton-build-app.log :as build-log]
   [babashka.process :as p]
   [clojure.string :as str]))

(def default-opts
  {:in :inherit
   :out :inherit})

(defn- get-stream
  [stream]
  (cond (string? stream) stream
        (instance? java.io.InputStream stream) (slurp stream)
        :else nil))

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
           (let [proc (-> process
                          deref)
                 exit-code (:exit proc)]
             [exit-code (str (get-stream (:out proc))
                             (get-stream (:err proc)))])))
       (catch Exception e
         (build-log/error-exception e)
         [-1 (str "Unexpected error during execution of this command" command)])))))
