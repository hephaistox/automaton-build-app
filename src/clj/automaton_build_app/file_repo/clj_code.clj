(ns automaton-build-app.file-repo.clj-code
  "Repository of files associating the name of a file to its content
  This repo deal with text files and will split the file in vector of lines"
  (:require
   [automaton-build-app.file-repo.text :as build-text-file-repo]
   [automaton-build-app.file-repo.raw.impl :as build-raw-impl]
   [automaton-build-app.file-repo.raw :as build-raw-file-repo]
   [automaton-build-app.os.files :as build-files]
   [clojure.string :as str]))

(defonce usage-to-extension
  {:clj [".clj"]
   :cljs [".cljs"]
   :cljc [".cljc"]
   :edn [".edn"]
   :clj-compiler [".clj" ".cljc"]
   :cljs-compiler [".cljc" ".cljs"]
   :code [".clj" ".cljc" ".cljs"]
   :reader [".clj" ".cljc" ".cljs" ".edn"]})

(defonce glob-code-extensions
  (format "**{%s}"
          (str/join ","
                    (:reader usage-to-extension))))

(defprotocol CodeRepo
  (filter-by-usage [this usage-kw] "Filter the existing files based on its usage, see `code-extenstions-map` for details"))

(defrecord CljCodeFilesRepository
    [_file-repo-map]

  build-raw-file-repo/FileRepository
  (exclude-files
    [_ excluded-files]
    (-> _file-repo-map
        (build-raw-impl/exclude-files excluded-files)
        ->CljCodeFilesRepository))

  (file-repo-map [_]
    _file-repo-map)

  (nb-files [_]
    (count _file-repo-map))

  (filter-repo
    [_ filter-fn]
    (-> (build-raw-impl/filter-repo-map _file-repo-map
                                        filter-fn)
        ->CljCodeFilesRepository))

  (filter-by-extension
    [_ extensions]
    (build-raw-impl/filter-by-extension _file-repo-map
                                        extensions))

  CodeRepo

  (filter-by-usage [_ usage-kw]
    (build-raw-impl/filter-repo-map _file-repo-map
                                    (fn [[filename _]]
                                      (some some?
                                            (map (partial build-files/match-extension? filename)
                                                 (get usage-to-extension
                                                      usage-kw)))))))

(defn search-clj-filenames
  "Return the list of clojure code file names
  Params:
  * `dir` the directory where files are searched"
  [dir]
  (build-files/search-files dir
                            glob-code-extensions))

(defn- match
  [filenames extensions-kw]
  (let [extensions (mapcat (fn [extension-kw]
                             (get usage-to-extension
                                  extension-kw))
                           extensions-kw)]
    (filter (fn [filename]
              (apply build-files/match-extension? filename extensions))
            filenames)))

(defn make-clj-repo
  "Build the repo while searching clj files in the directory `dir`
  Limit to the given extensions
  Params:
  * `dir`
  * `usage-ids` (Optional, default to `reader`) list of usage accepted, according to usage-to-extension definition"
  [dir & usage-ids]
  (let [usage-ids (or usage-ids
                      [:reader])]
    (-> (search-clj-filenames dir)
        (match usage-ids)
        build-text-file-repo/make-text-file-map
        ->CljCodeFilesRepository)))
