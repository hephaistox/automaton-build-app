(ns automaton-build-app.code-helpers.formatter
  "Format code
  Proxy to [zprint](https://github.com/kkinnear/zprint)"
  (:require [automaton-build-app.file-repo.clj-code :as build-clj-code]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.os.commands :as build-cmds]
            [automaton-build-app.os.files :as build-files]
            [automaton-build-app.utils.time :as build-time]
            [clojure.string :as str]))

(defn format-file
  "Format the `clj` or `edn` file
  Params:
  * `filename` to format
  * `header` (optional) is written at the top of the file"
  ([filename header]
   (let [format-content (slurp filename)]
     (build-files/spit-file
       filename
       (apply str
         [(when-not (str/blank? header)
            (print-str ";;" header (build-time/now-str) "\n")) format-content]))
     (build-cmds/execute-and-trace ["zprint" "-w" filename])))
  ([filename] (format-file filename nil)))

(defn code-files-formatted
  "Formats all clj* files to make sure they are compliant with our styling standards
  Params:
  * `clj-files-repo` is expected to be an instance of `build-clj-code/CljCodeFilesRepository`"
  [clj-files-repo]
  (let [clj-files (build-clj-code/filter-by-usage clj-files-repo :reader)]
    (doseq [file-name (keys clj-files)] (format-file file-name))
    (build-log/info "Files formatted")))
