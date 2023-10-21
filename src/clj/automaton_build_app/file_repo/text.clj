(ns automaton-build-app.file-repo.text
  "Repository of files associating the name of a file to its content
  This repo deal with text files and will split the file in vector of lines"
  (:require [automaton-build-app.file-repo.raw :as build-filerepo-raw]
            [automaton-build-app.file-repo.raw.impl :as build-raw-impl]
            [automaton-build-app.os.files :as build-files]
            [clojure.string :as str]))

(defrecord TextFileRepo [_file-repo-map]
  build-filerepo-raw/FileRepo
    (exclude-files [_ excluded-files]
      (-> _file-repo-map
          (build-raw-impl/exclude-files excluded-files)
          ->TextFileRepo))
    (file-repo-map [_] _file-repo-map)
    (filter-repo [_ filter-fn]
      (->> (build-raw-impl/filter-repo-map _file-repo-map filter-fn)
           ->TextFileRepo))
    (nb-files [_] (count _file-repo-map))
    (filter-by-extension [_ extensions]
      (-> _file-repo-map
          (build-raw-impl/filter-by-extension extensions)
          ->TextFileRepo)))

(defn make-text-file-map
  [filenames]
  (->> (build-raw-impl/repo-map filenames)
       (map (fn [[filename content]] [filename (str/split-lines content)]))
       (into {})))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn make-text-file-repo
  "Creates an instance of TextFileRepository"
  [filenames]
  (-> (make-text-file-map filenames)
      ->TextFileRepo))

(defn search-line
  "Search the pattern in a line
  Params:
  * `pattern` pattern to search
  * `file-line` data to search in"
  [pattern file-line]
  (re-find pattern file-line))

(defn filecontent-to-match
  "Apply the pattern to each line of each file of the repo
  The result is a vector containing as many elements as there are matches.
  Each element contains the filename where the match has been found, and the match itself
  Params:
  * `text-file-repo` an instance of `TextFilesRepository`
  * `pattern`"
  [text-file-repo pattern]
  (->> text-file-repo
       :_file-repo-map
       (mapcat (fn [[filename file-content]]
                 (->> file-content
                      (map (fn [file-line] [(build-files/relativize-to-pwd
                                              filename)
                                            (search-line pattern file-line)]))
                      (filter (comp not empty? second)))))
       vec))
