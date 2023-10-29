(ns automaton-build-app.la
  (:require [automaton-build-app.cli-test :as build-cli-test]))

(defn run
  "Defines tests to run"
  [selected-tasks cli-args]
  (-> (build-cli-test/select-tasks selected-tasks build-cli-test/cmds-to-test)
      (build-cli-test/cli-test cli-args)))

(comment
  (run ["clean"] {})
  ;
)
