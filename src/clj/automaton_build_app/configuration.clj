(ns automaton-build-app.configuration
  "Configuration parameters, stored in configuration file.
   This namespace is the entry point to call conf

  Design decision:
  * Configuration used to be based on outpace, it was too complicated for a small benefit,
  * The `config.edn` file was first unique, it needs to be updatable by environment to allow different
  value between production and repl, test and dev, but also monorepo vs app
  * The different version of parameter `config.edn` was first based on classpath (differentitated
  with aliases). It is ok for one app, but it renders the monrepo build complicated as it was naturally
  gathering all classpath, so all `config.edn` versions. The solution was to be based on environment
  parameter. So each alias can tell which version it uses, especially monorepo could be different.

  For a parameter `p`:
  * Create the parameter, in the current implementation, in the `util/conf.clj`
  * Read the parameter with  `conf/read-param`"
  (:require [automaton-build-app.configuration.protocol :as build-conf-prot]
            [automaton-build-app.configuration.simple-files :as simple-files]
            [automaton-build-app.log :as build-log]))

(defn start-conf
  []
  (try (build-log/debug "Starting configuration")
       (let [conf (simple-files/make-simple-conf)]
         (build-log/trace "Configuration is started")
         conf)
       (catch Throwable e
         (build-log/fatal-exception
           (ex-info "Configuration failed, application will stop" {:error e}))
         (throw e))))

(defn stop-conf [] (build-log/debug "Stop configuration"))

(def conf-state (start-conf))

(defn read-param
  ([key-path default-value]
   (if (not (vector? key-path))
     (do
       (build-log/warn-format
         "Key path should be a vector. I found `%s`, default value `%s` is returned"
         key-path
         default-value)
       default-value)
     (let [value (build-conf-prot/read-conf-param conf-state key-path)]
       (if (nil? value)
         (do (build-log/trace-format
               "Read key-path %s returned nil, defaulted to `%s`"
               key-path
               default-value)
             default-value)
         (do (build-log/trace "Read key-path " key-path " = " value) value)))))
  ([key-path] (read-param key-path nil)))
