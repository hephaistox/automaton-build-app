(ns automaton-build-app.code-helpers.clj-code
  "Proxy for clojure code files"
  (:require
   [automaton-build-app.os.files :as build-files]
   [clojure.string :as str]))

(def code-extensions
  [".clj"
   ".cljs"
   ".cljc"
   ".edn"])

(def glob-code-extensions
  (str "**{"
       (str/join ","
                 code-extensions)
       "}"))

(defn search-clj-filenames
  "Return the list of clojure code file names
  Params:
  * `dir` the directory where files are searched"
  [dir]
  (build-files/search-files dir
                            glob-code-extensions))

(defn is-clj-file
  "Return true if the file is a clj file"
  [filename]
  (when filename
    (some #(str/ends-with? filename
                           %)
          code-extensions)))
