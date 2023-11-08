(ns automaton-build-app.app
  (:require [automaton-build-app.code-helpers.build-config :as build-build-config]
            [automaton-build-app.code-helpers.deps-edn :as build-deps-edn]
            [automaton-build-app.code-helpers.frontend-compiler :as build-frontend-compiler]
            [automaton-build-app.os.files :as build-files]))

(def build-app-data
  "Build the map of the application data
  Will check the validity of the map regarding the schema, but is here for information only, the app will launch with that config anyway
  But a message will be logged
  Params:
  * `app-dir` the directory path of the application"
  (memoize build-build-config/build-app-data*))

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
  [{:keys [app-dir deps-edn]
    :as _app} limit-to-existing?]
  (let [paths (build-deps-edn/extract-paths deps-edn #{} limit-to-existing?)] (apply build-files/sorted-absolutize-dirs app-dir paths)))

(defn cljs-compiler-classpaths
  "Existing source directories for backend, as strings of absolutized directories"
  [{:keys [app-dir shadow-cljs]
    :as _app}]
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
