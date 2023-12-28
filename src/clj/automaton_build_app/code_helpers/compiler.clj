(ns automaton-build-app.code-helpers.compiler
  "Compiler of the project
  contains a clj version and a cljs version"
  (:require [automaton-build-app.app.deps-edn :as build-deps-edn]
            [automaton-build-app.cicd.version :as build-version]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.os.files :as build-files]
            [clojure.tools.build.api :as clj-build-api]
            [deps-deploy.deps-deploy :as deps-deploy]))

(defn clj-compiler
  "Compile the application.
  Returns the local jar filename which has been generated

  Params:
  * `app-name`
  * `app-dir`
  * `deps-edn`
  * `publication`
  * `env` - environment to compile"
  [app-name app-dir deps-edn publication env]
  (let [{:keys [as-lib major-version class-dir target-filename]} publication
        class-dir (format class-dir env)
        _ (prn "class-dir: " class-dir)
        target-filename (format target-filename env app-name)
        basis (clj-build-api/create-basis {:project build-deps-edn/deps-edn})
        app-paths (build-deps-edn/extract-paths deps-edn #{})
        version (build-version/version-to-push app-dir major-version)
        jar-file (format target-filename (name as-lib) version)
        app-source-paths (->> app-paths
                              (filter #(re-find #"src" %)))]
    (build-log/info "Launch clj compilation")
    (build-log/debug "Write POM files")
    (clj-build-api/write-pom {:class-dir class-dir
                              :lib as-lib
                              :version version
                              :basis basis
                              :src-dirs app-source-paths
                              :pom-data [[:licenses
       [:license
        [:name "Eclipse Public License 1.0"]
        [:url "https://opensource.org/license/epl-1-0/"]
        [:distribution "https://github.com/hephaistox/automaton-build-app"]]]]})
    (build-log/debug-format "Copy files from `%s` to `%s`" app-paths class-dir)
    (try (when (build-files/copy-files-or-dir app-paths class-dir)
           (build-log/debug-format "Jar is built `%s`" jar-file)
           (clj-build-api/jar {:class-dir class-dir
                               :jar-file jar-file})
           (build-log/info-format "Compilation ending successfully: `%s`" jar-file)
           jar-file)
         (catch Exception e (build-log/error-exception (ex-info "Compilation failed" {:exception e})) nil))))

(defn publish-to-clojars "Publish the jar [](https://github.com/slipset/deps-deploy)" []
  (deps-deploy/deploy  {:installer :remote
                        :artifact "target/prod/automaton-build-app.jar"
                        :sign-release? true
                        :pom-file "target/prod/class/META-INF/maven/org.clojars.hephaistox/automaton-build-app/pom.xml"


                        }))
