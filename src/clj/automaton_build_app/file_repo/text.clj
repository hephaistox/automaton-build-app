(ns automaton-build-app.file-repo.text
  "Repository of files associating the name of a file to its content
  This repo deal with text files and will split the file in vector of lines"
  (:require
   [automaton-build-app.file-repo.raw :as build-raw-file-repo]
   [automaton-build-app.file-repo.raw.impl :as build-raw-impl]
   [clojure.string :as str]))

(defrecord TextFilesRepository
           [_file-repo-map]
  build-raw-file-repo/FileRepository
  (exclude-files
    [_ excluded-files]
    (-> _file-repo-map
        (build-raw-impl/exclude-files excluded-files)
        ->TextFilesRepository))

  (file-repo-map [_]
    _file-repo-map)

  (filter-repo
    [_ filter-fn]
    (->> (build-raw-impl/filter-repo-map _file-repo-map
                                         filter-fn)
         ->TextFilesRepository))

  (nb-files [_]
    (count _file-repo-map))

  (filter-by-extension
    [_ extensions]
    (-> _file-repo-map
        (build-raw-impl/filter-by-extension extensions)
        ->TextFilesRepository)))

(defn make-text-file-map
  [filenames]
  (->> (build-raw-impl/repo-map filenames)
       (map (fn [[filename content]]
              [filename (str/split-lines content)]))
       (into {})))

(defn make-text-file-repo
  [filenames]
  (-> (make-text-file-map filenames)
      ->TextFilesRepository))
