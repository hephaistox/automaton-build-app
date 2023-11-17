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
