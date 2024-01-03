(ns automaton-build-app.code-helpers.compiler
  "Compiler of the project
  contains a clj version and a cljs version"
  (:require [automaton-build-app.app.deps-edn :as build-deps-edn]
            [automaton-build-app.cicd.version :as build-version]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.os.files :as build-files]
            [clojure.tools.build.api :as clj-build-api]
            [deps-deploy.deps-deploy :as deps-deploy]
            [automaton-build-app.configuration :as build-conf]))

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
                                          [:license [:name "Eclipse Public License 1.0"] [:url "https://opensource.org/license/epl-1-0/"]
                                           [:distribution "https://github.com/hephaistox/automaton-build-app"]]]]})
    (build-log/debug-format "Copy files from `%s` to `%s`" app-paths class-dir)
    (try (when (build-files/copy-files-or-dir app-paths class-dir)
           (build-log/debug-format "Jar is built `%s`" jar-file)
           (clj-build-api/jar {:class-dir class-dir
                               :jar-file jar-file})
           (build-log/info-format "Compilation ending successfully: `%s`" jar-file)
           jar-file)
         (catch Exception e (build-log/error-exception (ex-info "Compilation failed" {:exception e})) nil))))

(defn app-clj-compiler
  "Compile the application.
  Returns the local jar filename which has been generated

  Params:
  * `app-name`
  * `app-dir`
  * `deps-edn`
  * `publication`
  * `env` - environment to compile"
  [app-name app-dir deps-edn publication env]
  (let [{:keys [as-lib major-version class-dir target-filename jar-main]} publication
        class-dir (format class-dir env)
        target-filename (format target-filename env app-name)
        basis (clj-build-api/create-basis)
        app-paths (build-deps-edn/extract-paths deps-edn #{:env-development-repl :env-development-test :common-test})
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
                                          [:license [:name "Eclipse Public License 1.0"] [:url "https://opensource.org/license/epl-1-0/"]
                                           [:distribution "https://github.com/hephaistox/automaton-build-app"]]]]})
    (build-log/debug-format "Copy files from `%s` to `%s`" app-paths class-dir)
    (try (when (build-files/copy-files-or-dir app-paths class-dir)
           (build-files/copy-files-or-dir ["env/common_config.edn" "env/production/resources/"]
                                          (build-files/create-file-path class-dir app-name "resources"))
           (build-log/debug-format "Jar is built `%s`" jar-file)
           (clj-build-api/compile-clj {:basis basis
                                       :src-dirs ["src" "env/production/src"]
                                       :class-dir class-dir
                                       :java-opts ["-Dheph-conf=config.edn,common_config.edn"]})
           (clj-build-api/uber {:class-dir class-dir
                                :uber-file jar-file
                                :basis basis
                                :main jar-main})
           (build-log/info-format "Compilation ending successfully: `%s`" jar-file)
           jar-file)
         (catch Exception e (build-log/error-exception (ex-info "Compilation failed" {:exception e})) nil))))

(defn publish-to-clojars
  "Publish the jar [](https://github.com/slipset/deps-deploy)"
  [jar-path pom-path]
  (deps-deploy/deploy {:installer :remote
                       :artifact jar-path
                       :sign-release? true
                       :pom-file pom-path
                       :repository {"clojars" {:url "https://clojars.org/repo"
                                               :username (build-conf/read-param [:clojars-username])
                                               :password (build-conf/read-param [:clojars-password])}}})
  true)
