(ns automaton-build-app.code-helpers.analyze.common
  "Common features for analyzers, if a function is supposed to be used externally, please move it to `automton-build-app.code-helpers.analyze`"
  (:require
   [automaton-build-app.code-helpers.analyze.reports :as build-clj-code-reports]
   [automaton-build-app.file-repo.raw :as build-raw-files-repo]
   [automaton-build-app.file-repo.text-analyzis :as build-file-repo-text-analyzis]
   [automaton-build-app.file-repo.text-analyzis.regexp :as build-repo-text-analyzis-regexp]
   [automaton-build-app.utils.namespace :as build-namespace]))

(defn search-line
  "Proxy to code-files search-line function"
  [pattern line]
  (when pattern
    (build-repo-text-analyzis-regexp/search-line pattern line)))

(comment
  (defn execute-report
    "Returns a report data structure
  Params:
  * `clj-repo` a clj repo implementing `automaton-build-app.file-repo.clj-code/CljcCodeFilesRepository`
  * `report-id` one key of the `automaton-build-app.code-helpers.test-toolings/reports` map"
    [clj-repo report-id]
    (let [report (get build-clj-code-reports/reports
                      report-id)
          {:keys [filter-repo-with pattern ns-excluded pattern-match-to-report]} report
          excluded-files (into #{}
                               (map build-namespace/ns-to-file
                                    ns-excluded))
          report (-> clj-repo
                     (build-raw-files-repo/exclude-files excluded-files)
                     (build-raw-files-repo/filter-repo filter-repo-with)
                     (build-repo-text-analyzis-regexp/make-regexp-filerepo-matcher pattern))]
      (build-file-repo-text-analyzis/map-report report
                                                pattern-match-to-report))


    (defn save-report
      "Save the reports
  Params:
    * `report-execution` an execution of the report
    * `output-files` map associating report id to the output file "
      [report-execution output-files report-id]
      (let [report (get build-clj-code-reports/reports
                        report-id)
            {:keys [report-title conf-report-output-kw]} report
            report-filename (get output-files
                                 conf-report-output-kw)]
        (build-file-repo-text-analyzis/save-as-report report-execution
                                                      report-title report-filename))))

  (defn report-empty?
    "Is the report empty?
  Params:
    * `report-execution` an execution of the report"
    [report-execution]
    (build-file-repo-text-analyzis/is-empty? report-execution)))
