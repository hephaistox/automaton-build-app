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

(defn is-hephaistox-deps
  "For a deps entry, return true if the dependency is from hephaistox monorepo

  Params:
  * `dep` is a pair of value, as seen in the `:deps` map"
  [dep]
  (->> dep
       first
       namespace
       (= "hephaistox")))

(defn hephaistox-deps
  "Filter the
  Params:
  * `deps-edn` the deps-edn file content"
  [deps-edn]
  (->> deps-edn
       :deps
       (filter is-hephaistox-deps)
       keys
       vec))

(defn extract-paths
  "Extracts the `:paths` and `:extra-paths` from a given `deps.edn`
   e.g. {:run {...}}
  Params:
  * `deps-edn` deps.end content
  * `excluded-aliases` (Optional, default #{}) is a collection of aliases to exclude"
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

(defn extract-deps
  "Extract dependencies in a `deps.edn` file
  Params:
  * `deps-edn` is the content of the file to search dependencies in
  * `excluded-aliases` is a collection of aliases to exclude"
  [{:keys [deps aliases]
    :as _deps-edn} excluded-aliases]
  (let [selected-aliases (apply dissoc aliases excluded-aliases)]
    (->> selected-aliases
         (map (fn [[_ alias-defs]] (vals (select-keys alias-defs [:extra-deps :deps]))))
         (apply concat)
         (into {})
         (concat deps)
         (map (fn [[deps-name deps-map]] [deps-name deps-map])))))

(defn update-dep-local-root
  "Update the local root directories in a dependency map (of one lib)
  After the update, the local root path will be relative and starting from `base-dir`

  Params:
  * `base-dir` is the directory where you look at that app from
  * `dep-map` dep is a dependency map (of one lib)"
  [base-dir dep-map]
  (if (contains? dep-map :local/root) (update dep-map :local/root (partial build-files/create-dir-path base-dir)) dep-map))

(defn update-alias-local-root
  "Update the local root directories in an alias
  After the update, the local root path will be relative and starting from `base-dir`

  Params:
  * `base-dir` is the directory where you look at that app from
  * `alias-map` is the content of the alias as found in the `deps.edn` file"
  [base-dir alias-map]
  (cond-> alias-map
    (contains? alias-map :extra-deps) (update :extra-deps #(update-vals % (partial update-dep-local-root base-dir)))
    (contains? alias-map :deps) (update :deps #(update-vals % (partial update-dep-local-root base-dir)))))

(defn update-aliases-local-root
  "Update all aliases local root to be relative starting from base-dir
  Params:
  * `base-dir`
  * `aliases-map` the map describing the alias as seen in deps.edn"
  [base-dir aliases-map]
  (update-vals aliases-map (partial update-alias-local-root base-dir)))

(defn spit-deps-edn
  "Spit `content` in the filename path
  Params:
  * `app-dir`
  * `content`"
  ([app-dir content header] (build-edn-utils/spit-edn (get-deps-filename app-dir) content header))
  ([app-dir content] (spit-deps-edn app-dir content nil)))
