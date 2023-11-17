(ns automaton-build-app.tasks.docstring
  (:require [automaton-build-app.app :as build-app]
            [automaton-build-app.doc.docstring :as build-docstring]
            [automaton-build-app.log :as build-log]))

(defn docstring
  [_cli-opts
   {:keys [app-dir app-name build-config]
    :as app} _bb-edn-args]
  (let [code-doc (get-in build-config [:doc :code-doc] {})
        exclude-dirs (->> (get-in build-config [:doc :exclude-dirs] #{"resources"})
                          set)
        {:keys [title description dir]} code-doc
        app-dirs (->> (build-app/classpath-dirs app)
                      (filterv #(not (contains? exclude-dirs %))))]
    (if (empty? code-doc)
      (do (build-log/debug "doc string doc generation is skipped as no parameters are found") true)
      (build-docstring/docstring app-dir app-name app-dirs title description dir))))
