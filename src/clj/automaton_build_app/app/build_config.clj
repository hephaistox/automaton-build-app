(ns automaton-build-app.app.build-config
  "Manage `build-config.edn` file"
  (:require [automaton-build-app.os.edn-utils :as build-edn-utils]
            [automaton-build-app.log.files :as build-log-files]
            [automaton-build-app.os.files :as build-files]
            [automaton-build-app.schema :as build-schema]))

(def build-config-filename "build_config.edn")

(defn build-config-schema
  "Build config schema"
  [task-map]
  [:map {:closed true} [:app-name :string] [:monorepo {:optional true} [:map {:closed true} [:template-dir {:optional true} :string]]]
   [:task-shared
    [:map {:closed true} [:account {:default "hephaistox"} :string]
     [:gha
      [:map {:closed true} [:repo-branch {:default "main"} :string] [:repo-url {:default "git@github.com:hephaistox/gha_image.git"} :string]
       [:workflows {:default [".github/workflows/commit_validation.yml"]} [:vector :string]]]]
     [:mermaid-dir {:default "docs/code/"} :string] [:repl-aliases [:vector :keyword]]
     [:storage-datomic
      [:map [:datomic-root-dir {:default "~/.datomic/"} :string] [:datomic-dir-pattern {:default "datomic-pro-%s/"} :string]
       [:datomic-transactor-bin-path {:default "bin/transactor"} :string] [:datomic-ver :string]]]
     [:publication
      [:map {:closed true} [:as-lib :symbol] [:class-dir {:default "target/%s/class/"} :string]
       [:target-filename {:default "target/%s/%s.jar"} :string] [:major-version :string] [:repo :string]
       [:frontend {:optional true}
        [:map {:closed true} [:run-aliases {:optional true} [:vector :keyword]]
         [:compiled-styles-css {:default "resources/public/css/compiled/styles.css"} :string]
         [:custom-css {:default "resources/css/custom.css"} :string] [:main-css {:default "resources/css/main.css"} :string]
         [:tailwind-config {:default ["tailwind.config.js"]} [:vector :string]]]]]]]] [:tasks task-map]])

(defn read-build-config
  "Load the `build_config.edn` file of an app
  Returns the content even if it is not matching the schema

  Params:
  * `app-dir` root directory of the app where the `build_config.edn` file is expected"
  [app-dir]
  (some-> (build-files/create-file-path app-dir build-config-filename)
          build-files/is-existing-file?
          build-edn-utils/read-edn))

(defn search-for-build-configs-paths
  "Scan the directory to find build-config files, starting in the current directory
  Useful to discover applications
  Search in the local directory, useful for application repo
  and in subdir, useful for monorepo

  It is important not to search everywehere in the paths as `tmp` directories may contains unwanted `build_config.edn` files

  Params:
  * `root-dir`
  Returns the list of directories with `build_config.edn` in it"
  [root-dir]
  (->> (build-files/search-files root-dir (str "{" build-config-filename ",*/" build-config-filename ",*/*/" build-config-filename "}"))
       flatten
       (filterv (comp not nil?))))

(defn create-build-config-schema
  [task-schema]
  (let [schema (build-config-schema task-schema)]
    (build-log-files/save-debug-info "build_config_schema.edn" schema)
    schema))

(defn build-config-default-values
  [task-schema build-config]
  (let [schema (create-build-config-schema task-schema)
        config-with-default-values (build-schema/add-default-values schema build-config)]
    (build-log-files/save-debug-info "build_tmp_config.edn"
                                     config-with-default-values
                                     (format "This is the `%s` content completed with default values" build-config-filename))
    (build-schema/valid? schema config-with-default-values build-config-filename)
    config-with-default-values))
