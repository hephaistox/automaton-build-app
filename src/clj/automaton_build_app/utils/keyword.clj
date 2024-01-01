(ns automaton-build-app.utils.keyword
  "Utility functions for keywords."
  (:require [clojure.string :as str]))

(defn keywordize
  "Change string to appropriate clojure keyword"
  [s]
  (-> (str/lower-case s)
      (str/replace "_" "-")
      (str/replace "." "-")
      (keyword)))

(defn sanitize-key "Changes keyword to appropriate clojure keyword." [k] (let [s (keywordize (name k))] s))

(defn sanitize-map-keys
  "Changes all keywords in a map to appropriate clojure keys."
  [map]
  (reduce-kv (fn [acc key value]
               (let [new-key (if (keyword? key) (sanitize-key key) key)
                     new-val (if (map? value) (sanitize-map-keys value) value)]
                 (assoc acc new-key new-val)))
             {}
             map))
