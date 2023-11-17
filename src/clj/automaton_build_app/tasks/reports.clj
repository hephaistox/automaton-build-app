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
            [automaton-build-app.os.exit-codes :as build-exit-codes]
            [automaton-build-app.os.files :as build-files]))

(defn- code-stats
  [reports-cfgs app-dir]
  (let [filename (get-in reports-cfgs [:doc :code-stats :output-file] "docs/code/stats.md")]
    (->> (build-code-stats/line-numbers app-dir)
         (build-code-stats/stats-to-md filename))))

(defn- comment-report
  [[reports-cfgs clj-repo]]
  (let [filename (get-in reports-cfgs [:output-files :comments] "docs/code/comments.edn")
        matches (build-analyze-comments/comment-matches clj-repo)]
    (-> (build-analyze-comments/save-report matches filename)
        (build-analyze-comments/assert-empty filename))))

(defn- css-report
  [[reports-cfgs clj-repo]]
  (let [filename (get-in reports-cfgs [:output-files :css] "docs/code/css.edn")
        matches (build-analyze-css/css-matches clj-repo)]
    (-> (build-analyze-css/save-report matches filename)
        (build-analyze-css/assert-empty filename))))

(defn- alias-report
  [[reports-cfgs clj-repo]]
  (let [filename (get-in reports-cfgs [:output-files :alias] "docs/code/alias.edn")
        matches (build-analyze-alias/alias-matches clj-repo)]
    (-> (build-analyze-alias/save-report matches filename)
        (build-analyze-alias/assert-empty filename))))

(defn- namespace-report
  [[reports-cfgs clj-repo]]
  (let [filename (get-in reports-cfgs [:output-files :namespace] "docs/code/namespace.edn")
        matches (build-analyze-namespace/alias-matches clj-repo)]
    (-> (build-analyze-namespace/save-report matches filename)
        (build-analyze-namespace/assert-empty filename))))

(defn keyword-report
  [[reports-cfgs clj-repo]]
  (let [filename (get-in reports-cfgs [:output-files :forbidden-words] #{})
        forbidden-words (get-in reports-cfgs [:forbidden-words] #{})
        regexp (build-forbidden-words/coll-to-alternate-in-regexp forbidden-words)
        matches (some->> regexp
                         (build-forbidden-words/forbidden-words-matches clj-repo))]
    (-> (build-forbidden-words/save-report matches filename)
        (build-forbidden-words/assert-empty filename))))

(defn- shadow-report
  [app-dir reports-cfgs]
  (when-let [shadow-report-filename (get-in reports-cfgs [:output-files :shadow-size-opt] "doc/codes/code-size.edn")]
    (build-frontend-compiler/create-size-optimization-report app-dir shadow-report-filename)))

(defn reports
  "Build all the reports"
  [_cli-opts
   {:keys [app-dir]
    :as app} _bb-edn-args]
  (let [clj-repo (->> app
                      build-app/src-dirs
                      (filter build-files/is-existing-dir?)
                      build-clj-code/make-clj-repo-from-dirs)]
    (code-stats app app-dir)
    (shadow-report app-dir app)
    (when (->> (map boolean
                    ((juxt comment-report css-report alias-report namespace-report keyword-report)
                     [(get-in app [:build-config :doc :reports]) clj-repo]))
               (some true?))
      (System/exit build-exit-codes/rules-broken))))
