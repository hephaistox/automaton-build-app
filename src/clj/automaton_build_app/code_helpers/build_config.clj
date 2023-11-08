(ns automaton-build-app.code-helpers.build-config
  "Manage `build-config.edn` file"
  (:require [automaton-build-app.code-helpers.deps-edn :as build-deps-edn]
            [automaton-build-app.code-helpers.frontend-compiler :as build-frontend-compiler]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.os.edn-utils :as build-edn-utils]
            [automaton-build-app.os.files :as build-files]
            [automaton-build-app.schema :as build-schema]))

(def build-config-filename "build_config.edn")

(def build-config-schema
  "Build config schema"
  [:map {:closed true} [:app-name :string] [:build? {:optional true} :boolean] [:cust-app? {:optional true} :boolean]
   [:everything? {:optional true} :boolean] [:template-app? {:optional true} :boolean] [:doc? {:optional true} :boolean]
   [:frontend? {:optional true} :boolean]
   ;; This map is not closed, as monorepo features should not
   ;; be described here that data are here for convenience
   [:monorepo [:map {:closed true} [:app-dir :string]]]
   [:publication
    [:map {:closed true} [:repo [:map {:closed true} [:address :string] [:branch :string]]] [:as-lib {:optional true} :symbol]
     [:major-version {:optional true} :string]
     [:gha-container {:optional true}
      [:map {:closed true} [:repo-url :string] [:repo-branch :string] [:account :string] [:workflows [:vector :string]]]]
     [:shadow-cljs {:optional true} [:map {:closed true} [:target-build [:maybe :keyword]]]]
     [:jar {:optional true} [:map {:closed true} [:class-dir :string] [:excluded-aliases [:set :keyword]] [:target-filename :string]]]]]
   [:lconnect {:optional true} [:map {:closed true} [:aliases [:vector :keyword]]]]
   [:ltest {:optional true} [:map {:closed true} [:aliases [:vector :keyword]]]] [:la {:optional true} [:map {:closed true}]]
   [:bb-tasks {:optional true}
    [:map {:closed true} [:exclude-tasks {:optional true} [:set :symbol]] [:select-tasks {:optional true} [:vector :string]]]]
   [:customer-materials {:optional true} [:map {:closed true} [:html-dir :string] [:dir :string] [:pdf-dir :string]]]
   [:container-repo {:optional true} [:map {:closed true} [:account :string]]]
   [:doc {:optional true}
    [:map {:closed true} [:dir :string] [:archi [:map {:closed true} [:dir :string]]]
     [:reports [:map {:closed true} [:output-files [:map-of :keyword :string]] [:forbidden-words [:set :string]]]]
     [:code-stats [:map {:closed true} [:output-file :string]]]
     [:code-doc {:optional true} [:map {:closed true} [:dir :string] [:title :string] [:description :string]]]]]
   [:clean [:map {:closed true} [:compile-logs-dirs [:vector :string]]]]
   [:templating {:optional true} [:map {:closed true} [:app-title :string]]]])

(defn valid?
  "Validate the file build config matches the expected format
  Return false if not validated,
  Returns `app-build-config if validated``
  Params:
  * `app-build-config` content of the file to validate"
  [app-build-config]
  (build-schema/valid? build-config-schema app-build-config))

(defn search-for-build-config
  "Scan the directory to find `build-config.edn` files, which is useful to discover applications

  Search:
  * in current directory, so will work when called on application directly (like `automaton-*` or customer app)
  * in sub directory, so it will discover all customer applications for instances, like customer app
  * in sub directory level 2, so it will discover `automaton-*`

  It is important not to search in all subdirectories to prevent to return so temporary directories

  Params:
  * none
  Returns the list of directories with `build_config.edn` in it"
  ([config-path]
   (->> (build-files/search-files config-path (str "{" build-config-filename ",*/" build-config-filename ",*/*/" build-config-filename "}"))
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
      (do (build-log/trace-format "Read build configuration key %s, found `%s`" key-path value) value)
      (do (build-log/trace-format "Read build configuration key %s, not found, defaulted to value `%s`" key-path default-value)
          default-value))))

(defn build-app-data*
  ([app-dir]
   (build-log/debug-format "Build app data based on directory `%s`" app-dir)
   (let [app-data (read-build-config app-dir)]
     ;;It is made on purpose to still use the build_config.edn even if it
     ;;is not validated.
     (valid? app-data)
     (assoc app-data
            :app-dir app-dir
            :shadow-cljs (build-frontend-compiler/load-shadow-cljs app-dir)
            :deps-edn (build-deps-edn/load-deps-edn app-dir))))
  ([] (build-app-data* "")))
