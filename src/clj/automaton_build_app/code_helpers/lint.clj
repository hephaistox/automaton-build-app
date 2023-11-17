(ns automaton-build-app.code-helpers.lint
  "Code linter"
  (:require [automaton-build-app.os.commands :as build-cmds]
            [automaton-build-app.os.files :as build-files]
            [automaton-build-app.log :as build-log]))

(defn lint
  "Lint the code
  Params:
  * `debug?`
  * `dirs` list of dirs to lint"
  [debug? dirs]
  (let [dirs (filterv build-files/is-existing-dir? dirs)]
    (if (empty? dirs)
      (do (build-log/warn "No file to lint, no class paths found") true)
      (build-cmds/execute-and-trace (concat ["clj-kondo" "--lint"] dirs (when debug? ["--debug"]))))))
