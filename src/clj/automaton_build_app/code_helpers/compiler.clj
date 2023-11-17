(ns automaton-build-app.code-helpers.compiler
  "Compiler of the project
  contains a clj version and a cljs version"
  (:require [automaton-build-app.app.deps-edn :as build-deps-edn]
            [automaton-build-app.cicd.version :as build-version]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.os.files :as build-files]
            [clojure.tools.build.api :as clj-build-api]))

(defn clj-compiler
  "Compile the application.
  Returns the local jar filename which has been generated

  Params:
  * `app-dir`
  * `deps-edn`
  * `target-filename`
  * `as-lib`
  * `excluded-aliases` aliases which are not added in the clj
  * `class-dir`
  * `major-version`"
  [app-dir deps-edn target-filename as-lib excluded-aliases class-dir major-version]
  (let [basis (clj-build-api/create-basis {:project build-deps-edn/deps-edn})
        app-paths (build-deps-edn/extract-paths deps-edn excluded-aliases)
        version (build-version/version-to-push app-dir major-version)
        jar-file (format target-filename (name as-lib) version)
        app-source-paths (->> (build-deps-edn/extract-paths deps-edn excluded-aliases)
                              (filter #(re-find #"src" %)))]
    (build-log/info "Launch clj compilation")
    (build-log/debug "Write POM files")
    (clj-build-api/write-pom {:class-dir class-dir
                              :lib as-lib
                              :version version
                              :basis basis
                              :src-dirs app-source-paths})
    (build-log/debug-format "Copy files from `%s` to `%s`" app-paths class-dir)
    (try (when (build-files/copy-files-or-dir app-paths class-dir)
           (build-log/debug-format "Jar is built `%s`" jar-file)
           (clj-build-api/jar {:class-dir class-dir
                               :jar-file jar-file})
           (build-log/info-format "Compilation ending successfully: `%s`" jar-file)
           jar-file)
         (catch Exception e (build-log/error-exception (ex-info "Compilation failed" {:exception e})) nil))))

(defn publish-to-clojars "Publish the jar [](https://github.com/slipset/deps-deploy)" [])
