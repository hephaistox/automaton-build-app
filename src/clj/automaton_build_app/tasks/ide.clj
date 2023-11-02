(ns automaton-build-app.tasks.ide
  (:require [automaton-build-app.tasks.format-files :as build-format]
            [automaton-build-app.tasks.lint :as build-task-lint]
            [automaton-build-app.tasks.reports :as build-reports]))

(defn ide
  "All code for ide"
  [parsed-cli-opts]
  ((juxt build-reports/reports build-format/format-files build-task-lint/lint) parsed-cli-opts))
