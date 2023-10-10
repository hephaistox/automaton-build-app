(ns automaton-build-app.code-helpers.report
  "Crearte code helpers on the code"
  (:require
   [automaton-build-app.log :as build-log]
   [automaton-build-app.os.edn-utils :as build-edn-utils]))

(defn- search-line
  "Search the pattern in a line
  Params:
  * `pattern` pattern to search
  * `file-line` data to search in"
  [pattern file-line]
  (re-find pattern file-line))

(defn create-report
  "The report is a list of matches of `pattern` in each file of `file-repo`
  Params:
  * `files-repo` file repository
  * `pattern` pattern to search in all files"
  [files-repo pattern]
  (into []
        (mapcat (fn [[filename file-content]]
                  (filter (comp not empty? second)
                          (map (fn [file-line]
                                 [filename (search-line pattern file-line)])
                               file-content)))
                files-repo)))

(defn save-report
  "Save the report so that users can see the detailed results
  Params:
  * `report` a report created by `create-report`
  * `report-title` a human readable title of that report. Contains one `%s` parameter which will be replaced with the content of the report
  * `filename` where to store the report"
  [report report-title report-filename]
  (build-log/info report-title)
  (when-not (empty? report)
    (build-edn-utils/spit-edn report-filename
                              report))
  report)

(defn filter-report
  "Filter the `report` with the `filter-fn`
  Params:
  * `report` a report created with `create-report`
  * `filter-fn` a function that returns true if we keep the value, function has two parameters `filename` the name of the file and `matches` is the list of matches"
  [report filter-fn]
  (filter (fn [[filename matches]]
            (filter-fn filename
                       matches))
          report))

(defn map-report
  "Map the report to change each report line
  Params:
  * `report` a report created with `create-report`
  * `update-fn` to update a line, with filename and matches as parameters"
  [report update-fn]
  (map (fn [[filename matches]]
         (update-fn filename matches))
       report))

(defn group-by-report
  "Creates groups on the report,
  Params:
  * `report` a report created with `create-report`
  * The `group-by-fn` is used to create the aggregates with (group-by-fn [report-line])
  * The `aggregation` is used to create a vector, each line in the report for that group is called with (aggregation report-line) to tell what to keep for that record in the aggregates"
  [report group-by-fn aggregation empty-result]
  (->> (group-by group-by-fn
                 report)
       (map (fn [[group data]]
              (conj group
                    (reduce (fn [aggregated item]
                              (conj aggregated
                                    (aggregation item)))
                            empty-result
                            data))))))

(defn print-report
  "Apply the `printer` to each line of the `report`,
  Params:
  * `report` a report created with `create-report`
  * `printer` a function to print each line"
  [report printer]
  (doseq [report-line report]
    (printer report-line))
  report)

(defn assert-empty
  "Assert if the report is non empty
   Params:
  * `report` a report created with `create-report`
  * `title` the title to display if the report is not empty"
  [report title]
  (when-not (empty?  report)
    (throw (ex-info title
                    {:report report})))
  report)
