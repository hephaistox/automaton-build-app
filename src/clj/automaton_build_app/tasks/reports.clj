(ns automaton-build-app.tasks.reports
  (:require [automaton-build-app.app :as build-app]
            [automaton-build-app.code-helpers.analyze.alias-has-one-namespace :as build-analyze-alias]
            [automaton-build-app.code-helpers.analyze.comments :as build-analyze-comments]
            [automaton-build-app.code-helpers.analyze.css :as build-analyze-css]
            [automaton-build-app.code-helpers.analyze.forbidden-words :as build-forbidden-words]
            [automaton-build-app.code-helpers.analyze.namespace-has-one-alias :as build-analyze-namespace]
            [automaton-build-app.code-helpers.code-stats :as build-code-stats]
            [automaton-build-app.code-helpers.frontend-compiler :as build-frontend-compiler]
            [automaton-build-app.file-repo.clj-code :as build-clj-code]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.os.exit-codes :as build-exit-codes]))

(defn- code-stats
  [build-data]
  (let [filename (get-in build-data [:doc :code-stats :output-file] "docs/code/stats.md")]
    (->> (build-code-stats/line-numbers "")
         (build-code-stats/stats-to-md filename))))

(defn- comment-report
  [[build-data clj-repo]]
  (let [filename (get-in build-data [:doc :reports :output-files :comments] "docs/code/comments.edn")
        matches (build-analyze-comments/comment-matches clj-repo)]
    (-> (build-analyze-comments/save-report matches filename)
        (build-analyze-comments/assert-empty filename))))

(defn- css-report
  [[build-data clj-repo]]
  (let [filename (get-in build-data [:doc :reports :output-files :css] "docs/code/css.edn")
        matches (build-analyze-css/css-matches clj-repo)]
    (-> (build-analyze-css/save-report matches filename)
        (build-analyze-css/assert-empty filename))))

(defn- alias-report
  [[build-data clj-repo]]
  (let [filename (get-in build-data [:doc :reports :output-files :alias] "docs/code/alias.edn")
        matches (build-analyze-alias/alias-matches clj-repo)]
    (-> (build-analyze-alias/save-report matches filename)
        (build-analyze-alias/assert-empty filename))))

(defn- namespace-report
  [[build-data clj-repo]]
  (let [filename (get-in build-data [:doc :reports :output-files :namespace] "docs/code/namespace.edn")
        matches (build-analyze-namespace/alias-matches clj-repo)]
    (-> (build-analyze-namespace/save-report matches filename)
        (build-analyze-namespace/assert-empty filename))))

(defn keyword-report
  [[build-data clj-repo]]
  (let [filename (get-in build-data [:doc :reports :output-files :forbidden-words] #{})
        forbidden-words (get-in build-data [:doc :reports :forbidden-words] #{})
        regexp (build-forbidden-words/coll-to-alternate-in-regexp forbidden-words)
        matches (build-forbidden-words/forbidden-words-matches clj-repo regexp)]
    (-> (build-forbidden-words/save-report matches filename)
        (build-forbidden-words/assert-empty filename))))

(defn- shadow-report
  [app-dir build-data]
  (when-let [shadow-report-filename (get-in build-data [:doc :reports :output-files :shadow-size-opt] "doc/codes/code-size.edn")]
    (build-frontend-compiler/create-size-optimization-report app-dir shadow-report-filename)))

(defn reports
  "Build all the reports"
  [{:keys [min-level]
    :as _parsed-cli-opts}]
  (build-log/set-min-level! min-level)
  (let [app-dir ""
        build-data (@build-app/build-app-data_ app-dir)
        clj-repo (-> build-data
                     build-app/src-dirs
                     build-clj-code/make-clj-repo-from-dirs)
        _ (code-stats build-data)
        _ (shadow-report app-dir build-data)
        res (map boolean ((juxt comment-report css-report alias-report namespace-report keyword-report) [build-data clj-repo]))]
    (when (some true? res) (System/exit build-exit-codes/rules-broken))))
