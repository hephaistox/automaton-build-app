(ns automaton-build-app.apps.app
  (:require
   [automaton-build-app.code-helpers.deps-edn :as build-deps-edn]
   [automaton-build-app.file-repos.text-file-repos :as build-text-file-repos]
   [automaton-build-app.code-helpers.clj-code :as build-clj-code]
   [automaton-build-app.code-helpers.build-config :as build-build-config]
   [automaton-build-app.cicd.cljs-compiler :as build-cljs-compiler]
   [automaton-build-app.log :as build-log]
   [automaton-build-app.os.files :as build-files]
   [automaton-build-app.schema :as build-schema]))

(def cust-app-schema
  "Customer application specific schema"
  [[:publication {:optional true} [:map {:closed true}
                                   [:repo-address :string]
                                   [:repo-name :string]
                                   [:link {:optional true} :string]
                                   [:as-lib {:optional true} :symbol]
                                   [:branch :string]]]
   [:templating {:optional true} [:map {:closed true}
                                  [:app-title :string]]]
   [:customer-materials {:optional true} [:map {:closed true}
                                          [:html-dir :string]
                                          [:dir :string]
                                          [:pdf-dir :string]]]
   [:doc {:optional true} [:map {:closed true}
                           [:dir :string]
                           [:code-doc :map]]]])

(def app-build-config-schema
  "Application schema"
  (into []
        (concat [:map {:closed true}
                 [:app-name :string]

                 [:build? {:optional true} :boolean]
                 [:cust-app? {:optional true} :boolean]
                 [:everything? {:optional true} :boolean]
                 [:template-app? {:optional true} :boolean]
                 [:doc? {:optional true} :boolean]
                 [:frontend? {:optional true} :boolean]

                 ;; This map is not closed, as monorepo features should not be described here
                 ;; that data are here for convienience
                 [:monorepo
                  [:map [:app-dir :string]]]]
                cust-app-schema)))

(defn valid?
  "Validate the file build config matches the expected format
  Return false if not validated,
  Returns `app-build-config if validated``
  Params:
  * `app-build-config` content of the file to validate"
  [app-build-config]
  (build-schema/valid? app-build-config-schema
                       app-build-config))

(defn build-app-data
  "Build the map of the application data
  Params:
  * `app-dir` the directory path of the application"
  [app-dir]
  (build-log/debug-format "Build app data for `%s`" app-dir)
  (some-> (build-build-config/read-build-config app-dir)
          valid?
          (assoc :app-dir app-dir
                 :shadow-cljs (build-cljs-compiler/load-shadow-cljs app-dir)
                 :deps-edn (build-deps-edn/load-deps-edn app-dir))))

(defn is-cust-app-but-template?
  "True if `app-name` is matching a name from a customer application but template
  Params:
  * `app` application"
  [app]
  (and (:cust-app? app)
       (not (:template-app? app))))

(defn is-cust-app-but-everything?
  "True if `app-name` is matching a name from a customer application but everything
  Params:
  * `app` application"
  [app]
  (and (:cust-app? app)
       (not (:everything? app))))

(defn get-clj-c-src-dirs
  "Return absolutized directories of sources of `app`, only if they already exists !
  Params:
  * `app` is the app to get dir from"
  [{:keys [app-dir deps-edn]
    :as _app}]
  (->> deps-edn
       build-deps-edn/extract-paths
       (apply build-files/sorted-absolutize-dirs app-dir)))

(defn get-cljc-s-src-dirs
  "Existing source directories for backend, as strings of absolutized directories"
  [{:keys [app-dir shadow-cljs]
    :as _app}]
  (->> shadow-cljs
       build-cljs-compiler/extract-paths
       (apply build-files/sorted-absolutize-dirs app-dir)))

(defn get-clj-c-s-src-dirs
  "Existing source directories for  and back, as strings of absolutized directories
  Params:
  * `app` is the app to get dir from"
  [app]
  (->> (concat (get-clj-c-src-dirs app)
               (get-cljc-s-src-dirs app))
       dedupe
       sort
       (into [])))

(defn clj-c-s-files-repo
  "Creates a code files repository for application app, with all clj, cljc and cljs files in it"
  [app]
  (->> app
       get-clj-c-s-src-dirs
       (mapcat build-clj-code/search-clj-filenames)
       build-text-file-repos/load-repo))
