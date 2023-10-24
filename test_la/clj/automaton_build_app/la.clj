(ns automaton-build-app.la
  (:require
   [automaton-build-app.cli-test :as build-cli-test]))

(defn run
  "Defines tests to run"
  []
  (build-cli-test/cli-test build-cli-test/cmds-to-test))
