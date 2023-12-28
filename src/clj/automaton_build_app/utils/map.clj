(ns automaton-build-app.utils.map
  "Gather utility functions for maps"
  (:require [automaton-build-app.utils.comparators :as build-comparators]))

(defn sort-submap
  "Sort the elements of a submap in the map
  Params
  * `m` map
  * `ks` is a sequence of sequence of keys where the submap should be sorted"
  [m & kss]
  (if (empty? kss)
    m
    (let [[ks & rkss] kss]
      (-> (update-in m ks (partial into (sorted-map-by build-comparators/comparator-kw-symbol)))
          (recur rkss)))))

(defn deep-merge
  "Deep merge nested maps.
  Last map has higher priority

  This code comes from this [gist](https://gist.github.com/danielpcox/c70a8aa2c36766200a95)"
  [& maps]
  (apply merge-with (fn [& args] (if (every? #(or (map? %) (nil? %)) args) (apply deep-merge args) (last args))) maps))

(defn select-keys*
  "Like select-keys, but works on nested keys."
  [m v]
  (reduce (fn [aggregate next]
            (let [key-value (if (vector? next) [(last next) (get-in m next)] [next (get m next)])]
              (if (second key-value) (apply assoc aggregate key-value) aggregate)))
          {}
          v))
