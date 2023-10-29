(ns automaton-build-app.tasks.code-helpers
  "Code helpers"
  (:require [automaton-build-app.app :as build-app]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.code-helpers.formatter :as build-formatter]
            [automaton-build-app.os.commands :as build-cmds]))

(defn lconnect
  "Local connection to the code
  Params:
  * none"
  [_parsed-cli-opts]
  (let [app-dir ""
        app-data (@build-app/build-app-data_ app-dir)
        aliases (get-in app-data [:lconnect :aliases])]
    (build-log/info-format "Starting repl with aliases `%s`"
                           (apply str aliases))
    (build-cmds/execute-and-trace ["clojure" (apply str "-M" aliases)])))

(defn format-all
  "Format all code"
  [_parsed-cli-opts]
  (let [app-dir ""
        app-data (@build-app/build-app-data_ app-dir)
        src-paths (build-app/src-dirs app-data)]
    (apply build-formatter/format-all-app src-paths)))
