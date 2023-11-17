(ns automaton-build-app.code-helpers.analyze.comments
  "Analyze all comments in the code, forbid that comments so their publication is controlled"
  (:require [automaton-build-app.file-repo.text :as build-filerepo-text]
            [automaton-build-app.file-repo.raw :as build-filerepo-raw]
            [automaton-build-app.code-helpers.analyze.utils :as build-analyze-utils]
            [automaton-build-app.utils.namespace :as build-namespace]
            [automaton-build-app.log :as build-log]))

;;These are defined as a workaround, so it won't create false positive when you
;;search them with regexp
(defonce T (str "T" "O" "D" "O"))

(defonce D (str "D" "O" "N" "E"))

(defonce N (str "N" "O" "T" "E"))

(defonce F (str "F" "I" "X" "M" "E"))

(def comments-pattern
  "Dectect notes in comments"
  (-> (format ";;\\s*(?:%s|%s|%s|%s)(.*)$" T N D F)
      re-pattern))

(defn comment-matches
  "List code lines matching comments
  Params:
  * `clj-repo`"
  [clj-repo]
  (build-log/info "Comments analyzis")
  (let [ns-to-exclude (->> ['automaton-build-app.code-helpers.analyze.comments 'automaton-build-app.code-helpers.analyze.comments-test]
                           (map build-namespace/ns-to-file)
                           (into #{}))
        matches (-> clj-repo
                    (build-filerepo-raw/exclude-files ns-to-exclude)
                    (build-filerepo-text/filecontent-to-match comments-pattern))]
    (->> matches
         (map (fn [[filename [whole-match comment]]] [comment filename whole-match]))
         vec)))

(defn save-report
  [matches filename]
  (build-analyze-utils/save-report matches
                                   (format "List of forbidden comments")
                                   filename
                                   (fn [[comment filename match]] (format "%s -> [%s] -> %s" comment filename match))))

(defn assert-empty [matches filename] (build-analyze-utils/assert-empty matches filename (format "Some forbidden words are found")))
