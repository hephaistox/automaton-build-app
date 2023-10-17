(ns automaton-build-app.tasks.code-helpers
  "Code helpers"
  (:require
   [automaton-build-app.app :as build-app]
   [automaton-build-app.code-helpers.compiler :as build-compiler]
   [automaton-build-app.log :as build-log]
   [automaton-build-app.os.commands :as build-cmds]))

(defn lconnect
  "Local connection to the code
  Params:
  * `aliases` list of aliases to gather to start the app"
  [& _opts]
  (let [app-data (build-app/build-app-data "")
        aliases (get-in app-data
                        [:lconnect :aliases])]
    (build-log/info-format "Starting repl with aliases `%s`" (apply str aliases))
    (build-cmds/execute-and-trace ["clojure" (apply str "-M" aliases)])))

;;TODO Add frontend
(defn compile-to-jar
  "Compile the whole app, in production mode"
  [& _opts]
  (let [{:keys [publication deps-edn]} (build-app/build-app-data "")
        {:keys [as-lib jar]} publication
        {:keys [target-filename class-dir]} jar
        excluded-aliases #{}]
    (build-compiler/clj-compiler deps-edn target-filename as-lib excluded-aliases class-dir)))
