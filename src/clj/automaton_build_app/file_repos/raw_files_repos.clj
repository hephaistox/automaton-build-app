(ns automaton-build-app.file-repos.raw-files-repos
  "Repository of files associating the name of a file to its content"
  (:require
   [automaton-build-app.os.files :as build-files]))

(defn load-repo
  "Return a map of filename associated with their content, e.g.
  `{\"core.clj\" \"core.clj file content\"}`
  Params:
  * `filenames` list of filename to include in the repo"
  [filenames]
  (into {}
        (->> filenames
             (filter build-files/is-existing-file?)
             (map (fn [filename]
                    (let [abs-filename (build-files/absolutize filename)]
                      [(str abs-filename) (build-files/read-file abs-filename)])))
             (into {}))))

(defn exclude-files
  "Exclude in the `files-repo` the files in the `excluded-files` set of filenames
  Params:
  * `files-repo` repository to filter
  * `excluded-files` sequence of file names that will not be included in the filename"
  [files-repo excluded-files]
  (let [excluded-files (into #{} excluded-files)]
    (into {}
          (filter (fn [[filename]]
                    (not (contains? excluded-files
                                    (build-files/file-name filename))))
                  files-repo))))
