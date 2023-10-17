(ns automaton-build-app.file-repo.text-analyzis
  "Analyze the report interface")

(defprotocol TextRepoAnalysis
  (save-as-report [_ report-title report-filename] "Save the report so that users can see the detailed results\n  Params:\n  * `report` a report created by `create-report`\n  * `report-title` a human readable title of that report. Contains one `%s` parameter which will be replaced with the content of the report\n  * `filename` where to store the report")
  (filter-matches [_ filter-fn] "Filter the `report` with the `filter-fn`\n  Params:\n  * `report` a report created with `create-report`\n  * `filter-fn` a function that returns true if we keep the value, function has two parameters `filename` the name of the file and `matches` is the list of matches")
  (print-report [_ printer] "Apply the `printer` to each line of the `report`,\n  Params:\n  * `report` a report created with `create-report`\n  * `printer` a function to print each line, with side effects")
  (map-report [_ update-fn] "Map the report to change each report line\n  Params:\n  * `report` a report created with `create-report`\n  * `update-fn` to update a line, with filename and matches as parameters")
  (group-by-report [_ group-by-fn aggregation empty-result] "Creates groups on the report,\n  Params:\n  * `report` a report created with `create-report`\n  * The `group-by-fn` is used to create the aggregates with (group-by-fn [report-line])\n  * The `aggregation` is used to create a vector, each line in the report for that group is called with (aggregation report-line) to tell what to keep for that record in the aggregates")
  (is-empty? [_] "Returns true if the repo analysis is empty"))
