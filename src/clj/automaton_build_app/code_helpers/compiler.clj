(ns automaton-build-app.code-helpers.compiler
  "Compiler of the project
  contains a clj version and a cljs version"
  (:require [automaton-build-app.app.deps-edn :as build-deps-edn]
            [automaton-build-app.cicd.version :as build-version]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.os.files :as build-files]
            [clojure.tools.build.api :as clj-build-api]
            [deps-deploy.deps-deploy :as deps-deploy]
            [automaton-build-app.configuration :as build-conf]
            [automaton-build-app.cicd.cfg-mgt :as build-cfg-mgt]))

(defn eclipse-license [] [:license [:name "Eclipse Public License 1.0"] [:url "https://opensource.org/license/epl-1-0/"]])

(defn compile-jar
  [class-dir jar-file]
  (clj-build-api/jar {:class-dir class-dir
                      :jar-file jar-file}))

(defn compile-uber-jar
  [basis class-dir jar-file jar-main]
  (clj-build-api/compile-clj {:basis basis
                              :src-dirs ["src" "env/production/src"]
                              :class-dir class-dir
                              :java-opts ["-Dheph-conf=config.edn,common_config.edn"]})
  (clj-build-api/uber {:class-dir class-dir
                       :uber-file jar-file
                       :basis basis
                       :main jar-main}))

(defn clj-compiler
  "Compile code to jar or uber-jar based on `jar-type`."
  [jar-type publication env app-name deps-edn excluded-aliases app-dir pom-data]
  (let [{:keys [as-lib class-dir target-filename jar-main]} publication
        class-dir (format class-dir env)
        target-filename (format target-filename env app-name)
        basis (clj-build-api/create-basis)
        app-paths (build-deps-edn/extract-paths deps-edn excluded-aliases)
        version (build-version/current-version app-dir)
        jar-file (format target-filename (name as-lib) version)
        app-source-paths (->> app-paths
                              (filter #(re-find #"src" %)))]
    (build-log/info "Launch clj compilation")
    (build-log/debug "Write POM files")
    (clj-build-api/write-pom (merge {:class-dir class-dir
                                     :lib as-lib
                                     :version version
                                     :basis basis
                                     :src-dirs app-source-paths}
                                    (when pom-data pom-data)))
    (clj-build-api/write-pom (merge {:target app-dir
                                     :lib as-lib
                                     :version version
                                     :basis basis
                                     :src-dirs app-source-paths}
                                    (when pom-data {:pom-data pom-data})))
    (build-log/debug-format "Copy files from `%s` to `%s`" app-paths class-dir)
    (try (when (build-files/copy-files-or-dir app-paths class-dir)
           (build-log/debug-format "Jar is built `%s`" jar-file)
           (case jar-type
             :jar (compile-uber-jar basis class-dir jar-file jar-main)
             :uber-jar (compile-jar class-dir jar-file))
           (build-log/info-format "Compilation ending successfully: `%s`" jar-file)
           jar-file)
         (catch Exception e (build-log/error-exception (ex-info "Compilation failed" {:exception e})) nil))))

(defn lib-clj-compiler
  "Compile the application.
  Returns the local jar filename which has been generated

  Params:
  * `app-name`
  * `app-dir`
  * `deps-edn`
  * `publication`
  * `env` - environment to compile"
  [app-name app-dir deps-edn publication env]
  (clj-compiler :jar publication env app-name deps-edn #{} app-dir [[:licenses (eclipse-license)]]))

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
  (clj-compiler :uber-jar publication env app-name deps-edn #{:env-development-repl :env-development-test :common-test} app-dir nil))

(defn publish-library
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

(defn publish-app
  ([repo-uri app-dir]
   (let [clever-dir (build-files/create-dir-path app-dir ".clever")
         clever-repo-dir (build-files/create-dir-path clever-dir "repo")]
     (build-files/delete-files [clever-repo-dir])
     (build-files/ensure-directory-exists clever-repo-dir)
     (build-cfg-mgt/clone-repo-branch clever-dir "repo" repo-uri "master")
     (build-files/copy-files-or-dir ["target"] (build-files/create-dir-path clever-repo-dir "target"))
     (build-cfg-mgt/commit-and-push clever-repo-dir nil "master")))
  ([repo-uri] (publish-app repo-uri ".")))
