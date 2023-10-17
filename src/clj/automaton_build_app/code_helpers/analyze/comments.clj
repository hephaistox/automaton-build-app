(ns automaton-build-app.code-helpers.analyze.comments
  "Analyze all comments in the code, forbid that comments so their publication is controlled"
  (:require
   [automaton-build-app.file-repo.raw :as build-raw-files-repo]
   [automaton-build-app.file-repo.text-analyzis :as build-file-repo-text-analyzis]
   [automaton-build-app.file-repo.text-analyzis.regexp :as build-repo-text-analyzis-regexp]
   [automaton-build-app.utils.namespace :as build-namespace]))

;;These are defined as a workaround, so it won't create false positive when you search them with regexp
(defonce T
  (str "T" "O" "D" "O"))

(defonce D
  (str "D" "O" "N" "E"))

(defonce N
  (str "N" "O" "T" "E"))

(defonce F
  (str "F" "I" "X" "M" "E"))

(def regexp
  "Dectect notes in comments"
  (-> (format ";;\\s*(?:%s|%s|%s|%s)(.*)$"
              T
              N
              D
              F)
      re-pattern))

(defrecord CommentAnalyzer
           [clj-repo]
  build-file-repo-text-analyzis/TextRepoAnalysis

  (execute-report [_]
    (let [excluded-files (into #{}
                             (map build-namespace/ns-to-file
                                  ['automaton-build-app.code-helpers.analyze.comments
                                   'automaton-build-app.code-helpers.analyze.comments-test]))
        report (-> clj-repo
                   (build-raw-files-repo/exclude-files excluded-files)
                   (build-repo-text-analyzis-regexp/make-regexp-filerepo-matcher regexp))]
    (build-file-repo-text-analyzis/map-report report
                                              (fn [filename [_whole-match comment]]
                                                [comment filename]))))
  (save-report [_]
    )

  (is-empty? [_]
    ))
