(ns automaton-build-app.code-helpers.analyze.namespace-has-one-alias
  "Search for namespaces which are aliazed many times"
  (:require [automaton-build-app.code-helpers.analyze.utils :as
             build-analyze-utils]
            [automaton-build-app.os.edn-utils :as build-edn-utils]
            [automaton-build-app.os.files :as build-files]
            [automaton-build-app.file-repo.text :as build-filerepo-text]))

(def alias-pattern
  #"^\s*\[\s*([A-Za-z0-9\*\+\!\-\_\.\'\?<>=]*)\s*(?:(?::as)\s*([A-Za-z0-9\*\+\!\-\_\.\'\?<>=]*)|(:refer).*)\s*\]\s*(?:\)\))*$")

(defn- search-alias-with-multiple-namespaces
  [matches]
  (->> matches
       (group-by (fn [[_filename namespace alias :as _match]] [namespace
                                                               alias]))
       (mapv (fn [[k-alias-ns match]] [k-alias-ns (mapv first match)]))
       (group-by ffirst)
       (filter (fn [[_k-alias-ns files]] (> (count files) 1)))
       vals
       (apply concat)
       vec
       (group-by ffirst)))

(defn alias-matches
  "Creates the list of namespaces, their alias and filenames, save it in a report
  All namespaces with an alias are listed, except the sut alias as it is a special case

  Params:
  * `clj-repo`"
  [clj-repo]
  (let [matches (-> clj-repo
                    (build-filerepo-text/filecontent-to-match alias-pattern))]
    (->> matches
         (map (fn [[filename [_whole-match namespace alias _refer?]]] [filename
                                                                       namespace
                                                                       alias]))
         (filter (fn [[_filename namespace alias]]
                   (not (or (= "sut" alias)
                            (nil? alias)
                            (= "clojure.deftest" namespace)))))
         search-alias-with-multiple-namespaces)))

(defn save-report
  [matches filename]
  (build-edn-utils/spit-edn filename
                            matches
                            "List of aliases referencing many namespaces"))

(defn assert-empty
  [matches]
  (build-analyze-utils/assert-empty
    matches
    "Found namespace represented with more than one alias"))
