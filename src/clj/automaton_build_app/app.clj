(ns automaton-build-app.app
  (:require [automaton-build-app.code-helpers.deps-edn :as build-deps-edn]
            [automaton-build-app.code-helpers.build-config :as
             build-build-config]
            [automaton-build-app.code-helpers.frontend-compiler :as
             build-frontend-compiler]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.os.files :as build-files]
            [automaton-build-app.schema :as build-schema]))

(def cust-app-schema
  "Customer application specific schema"
  [[:publication
    [:map {:closed true}
     [:repo [:map {:closed true} [:address :string] [:branch :string]]]
     [:as-lib {:optional true} :symbol]
     [:major-version {:optional true} :string]
     [:gha-container {:optional true}
      [:map {:closed true} [:repo-url :string] [:repo-branch :string]
       [:account :string] [:workflows [:vector [:tuple :string :string]]]]]
     [:shadow-cljs {:optional true}
      [:map {:closed true} [:target-build [:maybe :keyword]]]]
     [:jar {:optional true}
      [:map {:closed true} [:class-dir :string]
       [:excluded-aliases [:set :keyword]] [:target-filename :string]]]]]
   [:lconnect {:optional true}
    [:map {:closed true} [:aliases [:vector :keyword]]]]
   [:ltest {:optional true} [:map {:closed true} [:aliases [:vector :keyword]]]]
   [:la {:optional true} [:map {:closed true}]]
   [:bb-tasks {:optional true}
    [:map {:closed true} [:exclude-tasks {:optional true} [:set :symbol]]
     [:select-tasks {:optional true} [:vector :string]]]]
   [:customer-materials {:optional true}
    [:map {:closed true} [:html-dir :string] [:dir :string] [:pdf-dir :string]]]
   [:container-repo {:optional true} [:map {:closed true} [:account :string]]]
   [:doc {:optional true}
    [:map {:closed true} [:dir :string]
     [:archi [:map {:closed true} [:dir :string]]]
     [:reports
      [:map {:closed true} [:output-files [:map-of :keyword :string]]
       [:forbidden-words [:set :string]]]]
     [:code-stats [:map {:closed true} [:output-file :string]]]
     [:code-doc {:optional true}
      [:map {:closed true} [:dir :string] [:title :string]
       [:description :string]]]]]
   [:clean [:map {:closed true} [:compile-logs-dirs [:vector :string]]]]
   [:templating {:optional true} [:map {:closed true} [:app-title :string]]]])

(def app-build-config-schema
  "Application schema"
  (into []
        (concat [:map {:closed true} [:app-name :string]
                 [:build? {:optional true} :boolean]
                 [:cust-app? {:optional true} :boolean]
                 [:everything? {:optional true} :boolean]
                 [:template-app? {:optional true} :boolean]
                 [:doc? {:optional true} :boolean]
                 [:frontend? {:optional true} :boolean]
                 ;; This map is not closed, as monorepo features should not
                 ;; be described here that data are here for convenience
                 [:monorepo [:map {:closed true} [:app-dir :string]]]]
                cust-app-schema)))

(defn valid?
  "Validate the file build config matches the expected format
  Return false if not validated,
  Returns `app-build-config if validated``
  Params:
  * `app-build-config` content of the file to validate"
  [app-build-config]
  (build-schema/valid? app-build-config-schema app-build-config))

(defn- build-app-data*
  ([app-dir]
   (build-log/debug-format "Build app data based on directory `%s`" app-dir)
   (let [app-data (build-build-config/read-build-config app-dir)]
     ;;It is made on purpose to still use the build_config.edn even if it
     ;;is not validated.
     (valid? app-data)
     (assoc app-data
       :app-dir app-dir
       :shadow-cljs (build-frontend-compiler/load-shadow-cljs app-dir)
       :deps-edn (build-deps-edn/load-deps-edn app-dir))))
  ([] (build-app-data* "")))

(def build-app-data_
  "Build the map of the application data
  Will check the validity of the map regarding the schema, but is here for information only, the app will launch with that config anyway
  But a message will be logged
  Params:
  * `app-dir` the directory path of the application"
  (delay (memoize build-app-data*)))

(defn is-cust-app-but-template?
  "True if `app-name` is matching a name from a customer application but template
  Params:
  * `app` application"
  [app]
  (and (:cust-app? app) (not (:template-app? app))))

(defn is-cust-app-but-everything?
  "True if `app-name` is matching a name from a customer application but everything
  Params:
  * `app` application"
  [app]
  (and (:cust-app? app) (not (:everything? app))))

(defn clj-compiler-classpath
  "Return absolutized directories of sources of `app`, only if they already exists !
  Params:
  * `app` is the app to get dir from
  * `limit-to-existing?` if "
  [{:keys [app-dir deps-edn], :as _app} limit-to-existing?]
  (let [paths (build-deps-edn/extract-paths deps-edn #{} limit-to-existing?)]
    (apply build-files/sorted-absolutize-dirs app-dir paths)))

(defn cljs-compiler-classpaths
  "Existing source directories for backend, as strings of absolutized directories"
  [{:keys [app-dir shadow-cljs], :as _app}]
  (->> shadow-cljs
       build-frontend-compiler/extract-paths
       (apply build-files/sorted-absolutize-dirs app-dir)))

(defn classpath-dirs
  "Existing source directories for front and back, as strings of absolutized directories
  Params:
  * `app` is the app to get dir from"
  [app]
  (->> (concat (clj-compiler-classpath app true) (cljs-compiler-classpaths app))
       dedupe
       sort
       (into [])))

(defn src-dirs
  "Existing source directories for front and back, as strings of absolutized directories
  Exclude resources
  Params:
  * `app` is the app to get dir from"
  [app]
  (->> (concat (clj-compiler-classpath app true) (cljs-compiler-classpaths app))
       dedupe
       (filter (fn [path] (not (contains? #{"resources"} path))))
       sort
       (into [])))
