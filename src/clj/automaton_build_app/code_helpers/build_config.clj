(ns automaton-build-app.code-helpers.build-config
  "Manage `build-config.edn` file"
  (:require [automaton-build-app.os.edn-utils :as build-edn-utils]
            [automaton-build-app.os.files :as build-files]
            [automaton-build-app.log :as build-log]))

(def build-config-filename "build_config.edn")

(defn search-for-build-config
  "Scan the directory to find `build-config.edn` files, which is useful to discover applications

  Search:
  * in current directory, so will work when called on application directly (like `automaton-core` or customer app)
  * in sub directory, so it will discover all customer applications for instances, like `landing`
  * in sub directory level 2, so it will discover `automaton-*`

  It is important not to search in all subdirectories to prevent to return so temporary directories

  Params:
  * none
  Returns the list of directories with `build_config.edn` in it"
  ([config-path]
   (->> (build-files/search-files config-path
                                  (str "{"
                                       build-config-filename
                                       ",*/"
                                       build-config-filename
                                       ",*/*/"
                                       build-config-filename
                                       "}"))
        flatten
        (filter (comp not nil?))))
  ([] (search-for-build-config "")))

(defn spit-build-config
  "Spit a build config file
  Params:
  * `app-dir` where to store the build_config file
  * `content` to spit
  * `msg` (optional) to add on the top of the file"
  ([app-dir content msg]
   (let [filename (build-files/create-file-path app-dir build-config-filename)]
     (build-edn-utils/spit-edn filename content msg)
     filename))
  ([app-dir content] (spit-build-config app-dir content nil)))

(defn read-build-config
  "Load the `build_config.edn` file of an app
  Params:
  * `app-dir` root directory of the app where the `build_config.edn` file is expected"
  [app-dir]
  (some-> (build-files/create-file-path app-dir build-config-filename)
          build-files/is-existing-file?
          build-edn-utils/read-edn))

(defn read-param
  "Read a data in the build configuration file"
  [key-path default-value]
  (let [value (get-in (read-build-config ".") key-path)]
    (if value
      (do (build-log/trace-format "Read build configuration key %s, found `%s`"
                                  key-path
                                  value)
          value)
      (do
        (build-log/trace-format
          "Read build configuration key %s, not found, defaulted to value `%s`"
          key-path
          default-value)
        default-value))))
