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
   (if (build-files/is-existing-file? filename)
     (let [format-content (slurp filename)]
       (build-files/spit-file
         filename
         (apply str
           [(when (and (string? header) (not (str/blank? header)))
              (print-str ";;" header (build-time/now-str) "\n"))
            format-content]))
       (build-cmds/execute-and-trace ["zprint" "-w" filename]))
     (build-log/warn-format "Can't format file `%s` as it's not found"
                            filename)))
  ([filename] (format-file filename nil)))

(defn code-files-formatted
  "Formats all clj* files to make sure they are compliant with our styling standards
  Params:
  * `clj-files-repo` is expected to be an instance of `build-clj-code/CljCodeFilesRepository`"
  [clj-files-repo]
  (let [clj-files (build-clj-code/filter-by-usage clj-files-repo :reader)
        formattings (map format-file (keys clj-files))]
    (build-log/info "Files formatted")
    (every? some? formattings)))

(defn format-dir
  "Source paths format directory

  Is the equivalent of manually run `d * -e clj -e cljs -e cljc -e edn -x zprint -w {}`
  Params:
  * src-paths list of directories to analyze"
  [& src-paths]
  (build-log/info-format "Format clojure files in directories `%s`" src-paths)
  (let [extensions-to-parse (interleave
                              (repeat "-e")
                              (map (fn [s] (subs s 1))
                                build-clj-code/all-reader-extensions))]
    (doseq [src-path src-paths]
      (build-cmds/execute-and-trace (concat ["fd"]
                                            extensions-to-parse
                                            ["-F" "." src-path "-x" "zprint"
                                             "-w" "{}"])))))

(defn format-all-app
  "Scan all applications file to
  Params:
  * `src-paths`"
  [& src-paths]
  (format-file "bb.edn")
  (format-file "build_config.edn")
  (format-file "deps.edn")
  (format-file "shadow-cljs.edn")
  (format-file "version.edn")
  (apply format-dir src-paths))
