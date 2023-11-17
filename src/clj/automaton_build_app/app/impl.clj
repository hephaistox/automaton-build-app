(ns automaton-build-app.app.impl
  (:require [automaton-build-app.app.deps-edn :as build-deps-edn]
            [automaton-build-app.code-helpers.frontend-compiler :as build-frontend-compiler]
            [automaton-build-app.os.files :as build-files]))

(defn clj-compiler-classpath
  "Return absolutized directories of sources of `app`, only if they already exists !
  Params:
  * `app` is the app to get dir from"
  [{:keys [app-dir deps-edn]
    :as _app}]
  (let [paths (build-deps-edn/extract-paths deps-edn #{})] (apply build-files/sorted-absolutize-dirs app-dir paths)))

(defn cljs-compiler-classpaths
  "Existing source directories for backend, as strings of absolutized directories"
  [{:keys [app-dir shadow-cljs]
    :as _app}]
  (->> shadow-cljs
       build-frontend-compiler/extract-paths
       (apply build-files/sorted-absolutize-dirs app-dir)))
