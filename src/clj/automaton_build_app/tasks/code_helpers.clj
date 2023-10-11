(ns automaton-build-app.tasks.code-helpers
  "Code helpers"
  (:require
   [automaton-build-app.log :as build-log]
   [automaton-build-app.os.commands :as build-cmds]
   [automaton-build-app.tasks.common :as build-tasks-common]))

(defn lint
  "Lint the code
  Params:
  * `debug?`
  * `dirs` list of dirs to lint"
  [debug? & dirs]
  (-> (build-cmds/execute (concat ["clj-kondo" "--lint"]
                                  dirs
                                  (when debug?
                                    "--debug")))
      last
      build-tasks-common/exit-code))

(defn lconnect
  "Local connection to the code
  Params:
  * `aliases` list of aliases to gather to start the app"
  [& aliases]
  (build-log/info-format "Starting repl with aliases `%s`" (apply str aliases))
  (-> (build-cmds/execute ["clojure" (apply str "-M" aliases)])
      last
      build-tasks-common/exit-code))

(defn update-deps
  "Update the dependencies of the project"
  []
  (-> (build-cmds/execute ["clojure" "-X" "automaton-build-app.code-helpers.update-deps/do-update"])
      last
      build-tasks-common/exit-code))

(defn code-doc
  "Create the code documentation"
  []
  (-> (build-cmds/execute ["clojure" "-X" "automaton-build-app.doc.code-doc/build-doc"])
      last
      build-tasks-common/exit-code))
