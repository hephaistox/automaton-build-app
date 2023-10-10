(ns automaton-build-app.cicd.cljs-compiler
  "Adapter for a cljs compiler
  Currently using shadow-cljs"
  (:require
   [automaton-build-app.os.files :as build-files]
   [automaton-build-app.os.edn-utils :as build-edn-utils]))

(def shadow-cljs-edn
  "shadow-cljs.edn")

(defn load-shadow-cljs
  "Read the shadow-cljs of an app
  Params:
  * `dir` the directory where to
  Returns the content as data structure"
  [dir]
  (some-> (build-files/create-file-path dir
                                        shadow-cljs-edn)
          build-files/is-existing-file?
          build-edn-utils/read-edn))

(defn extract-paths
  "Extract paths from the shadow cljs file content
  Params:
  * `shadow-cljs-content` is the content of a shadow-cljs file
  Return a flat vector of all source paths"
  [shadow-cljs-content]
  (:source-paths shadow-cljs-content))
