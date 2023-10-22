(ns automaton-build-app.file-repo.clj-code
  "Repository of files associating the name of a file to its content
  This repo deal with text files and will split the file in vector of lines"
  (:require [automaton-build-app.file-repo.text :as build-filerepo-text]
            [automaton-build-app.file-repo.raw.impl :as build-raw-impl]
            [automaton-build-app.file-repo.raw :as build-filerepo-raw]
            [automaton-build-app.os.files :as build-files]
            [clojure.string :as str]))

;; Match a usage of the code and list all concerned extensions
(defonce ^:private usage-to-extension
  {:clj [".clj"],
   :cljs [".cljs"],
   :cljc [".cljc"],
   :edn [".edn"],
   :clj-compiler [".clj" ".cljc"],
   :cljs-compiler [".cljc" ".cljs"],
   :code [".clj" ".cljc" ".cljs"],
   :reader [".clj" ".cljc" ".cljs" ".edn"]})

(defonce ^:private glob-code-extensions
  (format "**{%s}" (str/join "," (:reader usage-to-extension))))

(defprotocol CodeRepo
  (filter-by-usage [this usage-kw]
    "Filter the existing files based on its usage, see `code-extenstions-map` for details"))

(defrecord CljCodeFileRepo [_file-repo-map]
  build-filerepo-raw/FileRepo
    (exclude-files [_ exclude-files]
      (-> _file-repo-map
          (build-raw-impl/exclude-files exclude-files)
          ->CljCodeFileRepo))
    (file-repo-map [_] _file-repo-map)
    (nb-files [_] (count _file-repo-map))
    (filter-repo [_ filter-fn]
      (-> (build-raw-impl/filter-repo-map _file-repo-map filter-fn)
          ->CljCodeFileRepo))
    (filter-by-extension [_ extensions]
      (build-raw-impl/filter-by-extension _file-repo-map extensions))
  CodeRepo
    (filter-by-usage [_ usage-kw]
      (build-raw-impl/filter-repo-map
        _file-repo-map
        (fn [[filename _]]
          (some some?
                (map (partial build-files/match-extension? filename)
                  (get usage-to-extension usage-kw)))))))

(defn search-clj-filenames
  "Return the list of clojure code file names
  Params:
  * `dir` the directory where files are searched"
  [dir]
  (build-files/search-files dir glob-code-extensions))

(defn- match
  [filenames extensions-kw]
  (let [extensions (mapcat (fn [extension-kw]
                             (get usage-to-extension extension-kw))
                     extensions-kw)]
    (filter (fn [filename]
              (apply build-files/match-extension? filename extensions))
      filenames)))

(defn make-clj-repo-from-dirs
  "Build the repo while searching clj files in the directory `dir`
  Limit to the given extensions
  Params:
  * `dirs`
  * `usage-ids` (Optional, default to `reader`) list of usage accepted, according to usage-to-extension definition"
  [dirs & usage-ids]
  (let [usage-ids (or usage-ids [:reader])]
    (-> (mapcat (fn [dir]
                  (-> dir
                      search-clj-filenames
                      (match usage-ids)
                      build-filerepo-text/make-text-file-map))
                dirs)
        ->CljCodeFileRepo)))

