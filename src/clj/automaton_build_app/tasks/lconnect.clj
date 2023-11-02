(ns automaton-build-app.tasks.lconnect
  (:require [automaton-build-app.app :as build-app]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.os.commands :as build-cmds]))

(defn lconnect
  "Local connection to the code
  Params:
  * none"
  [{:keys [min-level details]
    :as _parsed-cli-opts}]
  (build-log/set-min-level! min-level)
  (build-log/set-details? details)
  (let [app-dir ""
        app-data (@build-app/build-app-data_ app-dir)
        aliases (get-in app-data [:lconnect :aliases])]
    (build-log/info-format "Starting repl with aliases `%s`" (apply str aliases))
    (build-cmds/execute-and-trace ["clojure" (apply str "-M" aliases)])))
