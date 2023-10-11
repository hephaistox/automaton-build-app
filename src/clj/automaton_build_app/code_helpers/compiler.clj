(ns automaton-build-app.code-helpers.compiler
  "Compiler of the project
  contains a clj version and a cljs version"
  (:require
   [clojure.tools.build.api :as build-build-api]
   [automaton-build-app.code-helpers.deps-edn :as build-deps-edn]
   [automaton-build-app.apps.app :as build-app]
   [automaton-build-app.os.files :as build-files]))

(defn clj-compiler
  "Compile the application
  Params:
  * `excluded-aliases` aliases which are"
  [excluded-aliases]
  (let [{:keys [publication deps-edn]} (build-app/build-app-data "")
        {:keys [as-lib major-version jar]} publication
        {:keys [target-filename  class-dir]} jar

        basis (build-build-api/create-basis {:project build-deps-edn/deps-edn})
        version (format major-version (build-build-api/git-count-revs nil))
        jar-file (format target-filename (name as-lib) version)
        app-paths (build-deps-edn/extract-paths deps-edn
                                                (into #{} excluded-aliases))
        app-source-paths (filter #(re-find #"src" %)
                                 app-paths)]
    (build-build-api/write-pom {:class-dir class-dir
                                :lib as-lib
                                :version version
                                :basis basis
                                :src-dirs app-source-paths})
    (build-files/copy-files-or-dir app-paths
                                   class-dir)
    (build-build-api/jar {:class-dir class-dir
                          :jar-file jar-file})))
(comment
  (clj-compiler #{:common-test :env-development-test})
;
  )
