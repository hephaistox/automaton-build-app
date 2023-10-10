(ns automaton-build-app.file-repos.text-file-repos
  (:require
   [automaton-build-app.file-repos.raw-files-repos :as build-raw-files-repos]
   [clojure.string :as str]))

(defn load-repo
  "Return a map of filename associated with their content, e.g.
  `{\"core.clj\" \"core.clj file content\"}`
  Params:
  * `filenames` list of filename to include in the repo"
  [filenames]
  (->> (build-raw-files-repos/load-repo filenames)
       (map (fn [[filename content]]
              [filename (str/split-lines content)]))
       (into {})))
