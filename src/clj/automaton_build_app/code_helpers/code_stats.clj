(ns automaton-build-app.code-helpers.code-stats
  "Compute code statistics.
  Right now is counting how much core line and tests line there are"
  (:require [automaton-build-app.doc.markdown :as build-markdown]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.os.files :as build-files]
            [clojure.string :as str]))

(defn count-lines
  "Count lines in the file list"
  ([files reader] (count-lines files reader build-files/is-existing-file?))
  ([files reader check-existence-fn]
   (let [files (map str files)
         files-only (filter check-existence-fn files)]
     (reduce +
             (map (fn [file]
                    (let [content (reader file)]
                      (-> content
                          str/split-lines
                          count)))
                  files-only)))))

(defn line-numbers
  "Build statistics on the clojure code
  Params:
  * `stats-root-dir` directory the code is analyzed
  :clj-test-nb-lines : is the total number of lines in test
  :clj-total-nb-lines: total number of lines, clj(c,s) test and not test
  :ratio-in-pct: ratio between both.
     - 100% means there are the same number of test lines than src lines
     - 50% means there are double number of lines src than test ones"
  [stats-root-dir]
  (build-log/info "Code statistics report")
  (let [clj-total-nb-lines (count-lines (build-files/search-files stats-root-dir "**{.clj,.cljs,.cljc}") slurp)
        clj-test-nb-lines (count-lines (build-files/search-files stats-root-dir "**{_test.clj,_test.cljs,_test.cljc}") slurp)
        clj-src-nb-lines (- clj-total-nb-lines clj-test-nb-lines)]
    {:clj-test-nb-lines clj-test-nb-lines
     :clj-total-nb-lines clj-total-nb-lines
     :ratio-in-pct (/ (Math/floor (* 10000 (/ clj-test-nb-lines clj-src-nb-lines))) 100)}))

(defn stats-to-md
  "Create markdown from stats
  Params:
  * `filename` md file that will be generated
  * `line-numbers` result of `line-numbers`"
  [filename line-numbers]
  (build-log/debug "Generate statistics documentation for the monorepo in " filename)
  (build-markdown/create-md filename
                            (concat ["# That statistics counts number of line of code"]
                                    (sort (map (fn [[k v]] (str "* " (name k) " - " v)) line-numbers)))))
