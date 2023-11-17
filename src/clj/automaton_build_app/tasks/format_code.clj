(ns automaton-build-app.tasks.format-code
  (:require [automaton-build-app.app :as build-app]
            [automaton-build-app.code-helpers.formatter :as build-code-formatter]))

(defn format-code
  "Format all code files"
  [_cli-opts app _bb-edn-args]
  (let [src-paths (build-app/src-dirs app)
        exclude-dirs (->> (get-in app [:build-config :format-code :exclude-dirs] #{"resources"})
                          set)]
    (->> src-paths
         (filterv #(not (contains? exclude-dirs %)))
         (apply build-code-formatter/format-all-app))))
