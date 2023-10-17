(ns automaton-build-app.code-helpers.lint
  "Code linter"
  (:require
    [automaton-build-app.os.commands :as build-cmds]))

(defn lint
  "Lint the code
  Params:
  * `debug?`
  * `dirs` list of dirs to lint"
  [debug? dirs]
  (build-cmds/execute-and-trace (concat ["clj-kondo" "--lint"]
                                        dirs
                                        (when debug?
                                          "--debug"))))
