(ns automaton-build-app.code-helpers.analyze.forbidden-words
  "Search for keywords in the project"
  (:require [automaton-build-app.code-helpers.analyze.utils :as build-analyze-utils]
            [automaton-build-app.file-repo.text :as build-filerepo-text]
            [clojure.string :as str]
            [automaton-build-app.log :as build-log]))

(defn coll-to-alternate-in-regexp
  "Turn a collection of strings (or patterns) like (\"a\" \"b\") to \"(a|b)\"
  Params:
  * `coll` collection to transform. Each element could be a string or a regexp"
  [coll]
  (if (empty? coll)
    nil
    (->> coll
         (map str)
         (str/join "|")
         (format "(%s)")
         re-pattern)))

(defn forbidden-words-matches
  "Creates the list of namespaces, their alias and filenames, save it in a report
  All namespaces with an alias are listed, except the sut alias as it is a special case

  Returns the matches

  Params:
  * `clj-repo`
  * `regexp` regexp (with groups) of strings to search"
  [clj-repo regexp]
  (build-log/info "Forbidden words analyzis")
  (let [matches (build-filerepo-text/filecontent-to-match clj-repo regexp)]
    (->> matches
         (mapv (fn [[filename [whole-match & matches]]] [filename (vec matches) whole-match])))))

(defn save-report [matches filename] (build-analyze-utils/save-report matches "List of forbidden words found in the project" filename str))

(defn assert-empty [matches filename] (build-analyze-utils/assert-empty matches filename "That words should not appear in the project"))
