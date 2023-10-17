(ns automaton-build-app.file-repo.text-analyzis.regexp
  "Create a file repository based on regexp"
  (:require
   [automaton-build-app.file-repo.raw :as build-raw-files-repo]
   [automaton-build-app.file-repo.text-analyzis :as build-file-repo-text-analyzis]
   [automaton-build-app.log :as build-log]
   [automaton-build-app.os.edn-utils :as build-edn-utils]
   [clojure.string :as str]))

(defn search-line
  "Search the pattern in a line
  Params:
  * `pattern` pattern to search
  * `file-line` data to search in"
  [pattern file-line]
  (re-find pattern file-line))

(defrecord RegexpFileRepoMatcher
           [matching-text-lines]
  build-file-repo-text-analyzis/TextRepoAnalysis

  (save-as-report [this report-title report-filename]
    (when-not (str/blank? report-filename)
      (build-log/info-format "Save report `%s`" report-title)
      (when-not (empty? matching-text-lines)
        (build-edn-utils/spit-edn report-filename
                                  matching-text-lines)))
    this)

  (filter-matches [_ filter-fn]
    (->> matching-text-lines
         (filter (fn [[filename matches]]
                   (filter-fn filename
                              matches)))
         ->RegexpFileRepoMatcher))

  (map-report [_ update-fn]
    (->> matching-text-lines
         (map (fn [[filename matches]]
                (update-fn filename matches)))
         ->RegexpFileRepoMatcher))

  (group-by-report [_ group-by-fn aggregation empty-result]
    (->> matching-text-lines
         (group-by group-by-fn)
         (map (fn [[group data]]
                (conj group
                      (reduce (fn [aggregated item]
                                (conj aggregated
                                      (aggregation item)))
                              empty-result
                              data))))))

  (print-report [matching-text-lines printer]
    (doseq [matching-text-line matching-text-lines]
      (printer matching-text-line))
    (->RegexpFileRepoMatcher matching-text-lines))

  (is-empty? [_]
    (empty? matching-text-lines)))

(defn make-regexp-filerepo-matcher
  "Start a text-file-repo analyzis"
  [text-file-repo pattern]
  (->> text-file-repo
       build-raw-files-repo/file-repo-map
       (mapcat (fn [[filename file-content]]
                 (->> file-content
                      (map (fn [file-line]
                             [filename (search-line pattern file-line)]))
                      (filter (comp not empty? second)))))
       vec
       ->RegexpFileRepoMatcher))
