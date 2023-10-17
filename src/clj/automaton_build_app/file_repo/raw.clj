(ns automaton-build-app.file-repo.raw
  "Repository of files associating the name of a file to its content
  This repo doesn't assume anything about the file content, it just slurp it"
  (:require
   [automaton-build-app.file-repo.raw.impl :as build-raw-impl]))

(defprotocol FileRepository
  (exclude-files [this excluded-files] "Return a File repository without the files in `excluded-files`\n Params:\n * `files-repo` repository to filter\n * `excluded-files` sequence of file names that will not be included in the filename")
  (file-repo-map [this] "The map associating filepath with their content")
  (nb-files [this] "Return the number of embedded files")
  (filter-by-extension [this extensions] "Filter the repo on files matching the extension")
  (filter-repo [this filter-fn] "Filter the repo files\n Params: \n * `repo` repository to filter \n * `filter-fn` function taking the pair [filename file-content] as an argument and returning true if we want to keep that file in the repo. If nil is provided, the whole repo is returned"))

(defrecord RawFilesRepository
           [_file-repo-map]
  FileRepository

  (exclude-files
    [_ excluded-files]
    (-> _file-repo-map
        (build-raw-impl/exclude-files excluded-files)
        ->RawFilesRepository))

  (file-repo-map [_]
    _file-repo-map)

  (filter-repo
    [_ filter-fn]
    (-> (build-raw-impl/filter-repo-map _file-repo-map
                      filter-fn)
        ->RawFilesRepository))

  (nb-files [_]
    (count _file-repo-map))

  (filter-by-extension
    [_ extensions]
    (-> _file-repo-map
        (build-raw-impl/filter-by-extension extensions)
        ->RawFilesRepository)))

(defn make-raw-files-repo
  "Return a map of filename associated with their content, e.g.
  `{\"core.clj\" \"core.clj file content\"}`
  Params:
  * `filenames` list of filename to include in the repo, non existing files will be skipped"
  [filenames]
  (->RawFilesRepository (build-raw-impl/repo-map filenames)))
