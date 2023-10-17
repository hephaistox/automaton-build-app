(ns automaton-build-app.code-helpers.compiler
  "Compiler of the project
  contains a clj version and a cljs version"
  (:require
   [clojure.tools.build.api :as build-build-api]
   [automaton-build-app.code-helpers.deps-edn :as build-deps-edn]
   [automaton-build-app.log :as build-log]
   [automaton-build-app.cicd.version :as build-version]
   [automaton-build-app.os.files :as build-files]))

(defn clj-compiler
  "Compile the application.
  Returns the local jar filename which has been generated

  Params:
  * `excluded-aliases` aliases which are"
  [deps-edn target-filename as-lib excluded-aliases class-dir]
  (let [basis (build-build-api/create-basis {:project build-deps-edn/deps-edn})
        version (build-version/version-to-push)
        jar-file (format target-filename (name as-lib) version)
        app-paths (build-deps-edn/extract-paths deps-edn
                                                excluded-aliases
                                                true)
        app-source-paths (build-deps-edn/extract-src-paths deps-edn
                                                           excluded-aliases
                                                           true)]
    (build-log/info "Launch clj compilation")
    (build-log/debug "Write POM files")
    (build-build-api/write-pom {:class-dir class-dir
                                :lib as-lib
                                :version version
                                :basis basis
                                :src-dirs app-source-paths})
    (build-log/debug-format "Copy files from `%s` to `%s`" app-paths class-dir)
    (build-files/copy-files-or-dir app-paths
                                   class-dir)
    (build-log/debug-format "Jar is built `%s`" jar-file)
    (build-build-api/jar {:class-dir class-dir
                          :jar-file jar-file})
    (build-log/debug-format "Compilation ending successfully: `%s`" jar-file)
    jar-file))

(defn publish-to-clojars
  "Publish the jar [](https://github.com/slipset/deps-deploy)"
  [])
