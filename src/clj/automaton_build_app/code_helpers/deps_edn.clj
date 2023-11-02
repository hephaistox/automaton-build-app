(ns automaton-build-app.code-helpers.deps-edn
  "Proxy for `deps.edn` file"
  (:require [automaton-build-app.os.files :as build-files]
            [automaton-build-app.os.edn-utils :as build-edn-utils]))

(def deps-edn "deps.edn")

(defn get-deps-filename
  "Get the deps-file of the application
  Params:
  * `app-dir` is where the application is stored"
  [app-dir]
  (build-files/create-file-path app-dir deps-edn))
(defn load-deps-edn
  "Load the deps.edn file of the app, passed as a parameter,
  Params:
  * `app-dir` the directory of the app, where `deps.edn` is stored
  Returns nil if the file does not exists or is malformed"
  [app-dir]
  (build-edn-utils/read-edn (get-deps-filename app-dir)))

(defn extract-paths
  "Extracts the `:paths` and `:extra-paths` from a given `deps.edn`
   e.g. {:run {...}}
  Params:
  * `deps-edn` content the deps edn file to search extract path in
  * `excluded-aliases` (Optional, default #{}) is a collection of aliases to exclude
  * `limit-to-existing?` (Optional, default true) if true remove non existing directories"
  ([{:keys [paths aliases]
     :as _deps-edn} excluded-aliases limit-to-existing?]
   (let [selected-aliases (apply dissoc aliases excluded-aliases)
         paths-in-aliases (mapcat (fn [[_alias-name paths]] (apply concat (vals (select-keys paths [:extra-paths :paths]))))
                           selected-aliases)]
     (->> paths-in-aliases
          (concat paths)
          (filter (fn [file] (or (not limit-to-existing?) (build-files/is-existing-dir? file))))
          sort
          dedupe
          (into []))))
  ([deps-edn] (extract-paths deps-edn #{} true)))

(defn extract-src-paths
  "Extracts the `:paths` and `:extra-paths` from a given `deps.edn`, limit to source files (so exclude the resources)
   e.g. {:run {...}}
  Params:
  * `deps-edn` content the deps edn file to search extract path in
  * `excluded-aliases` is a collection of aliases to exclude
  * `limit-to-existing?` (Optional, default true) if true remove non existing directories"
  [deps-edn excluded-aliases limit-to-existing?]
  (->> (extract-paths deps-edn excluded-aliases limit-to-existing?)
       (filter #(re-find #"src" %))))
