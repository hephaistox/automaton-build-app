(ns automaton-build-app.app.deps-edn
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
  (some-> app-dir
          get-deps-filename
          build-files/is-existing-file?
          build-edn-utils/read-edn))

(defn extract-paths
  "Extracts the `:paths` and `:extra-paths` from a given `deps.edn`
   e.g. {:run {...}}
  Params:
  * `deps-edn` content the deps edn file to search extract path in
  * `excluded-aliases` (Optional, default #{}) is a collection of aliases to exclude
  * `limit-to-existing?` (Optional, default true) if true remove non existing directories"
  [{:keys [paths aliases]
    :as _deps-edn} excluded-aliases]
  (let [selected-aliases (apply dissoc aliases excluded-aliases)
        paths-in-aliases (mapcat (fn [[_alias-name alias-map]]
                                   (->> (select-keys alias-map [:extra-paths :paths])
                                        vals
                                        (apply concat)))
                          selected-aliases)]
    (->> paths-in-aliases
         (concat paths)
         sort
         dedupe
         (into []))))
