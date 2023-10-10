(ns automaton-build-app.code-helpers.formatter
  "Format code
  Proxy to [zprint](https://github.com/kkinnear/zprint)"
  (:require
   [automaton-build-app.os.files :as build-files]
   [automaton-build-app.os.commands :as build-cmds]
   [automaton-build-app.utils.time :as build-time]
   [clojure.string :as str]))

(defn format-file
  "Format the `clj` or `edn` file
  Params:
  * `filename` to format
  * `header` (optional) is written at the top of the file"
  ([filename header]
   (let [format-content (slurp filename)]
     (build-files/spit-file filename
                            (apply str [(when-not (str/blank? header)
                                          (print-str ";;" header (build-time/now-str) "\n"))
                                        format-content]))
     (build-cmds/execute ["zprint" filename])))
  ([filename]
   (format-file filename nil)))
