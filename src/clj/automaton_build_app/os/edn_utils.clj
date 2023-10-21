(ns automaton-build-app.os.edn-utils
  "Adapter to read an edn file"
  (:require [automaton-build-app.code-helpers.formatter :as
             build-code-formatter]
            [automaton-build-app.os.files :as build-files]
            [automaton-build-app.log :as build-log]
            [clojure.edn :as edn]))

(defn read-edn
  "Read the `.edn` file,
  Params:
  * `edn-filename` name of the edn file to load
  * `loader-fn` (Optional, default: files/read-file) a function returning the content of the file"
  [edn-filename]
  (try (let [edn-filename (build-files/absolutize edn-filename)
             edn-content (build-files/read-file edn-filename)]
         (edn/read-string edn-content))
       (catch Exception e
         (build-log/error-exception
           (ex-info (format "File `%s` is not an edn" edn-filename)
                    {:caused-by e, :file-name edn-filename}))
         nil)))

(defn spit-edn
  "Spit the `content` in the edn file called `deps-edn-filename`.
  If any, the header is added at the top of the file
  Params:
  * `edn-filename` Filename
  * `content` What is spitted
  * `header` the header that is added to the content, followed by the timestamp - is automatically preceded with ;;
  Return the content of the file"
  ([edn-filename content header]
   (try (build-log/trace "Spit edn file:" edn-filename)
        (build-files/spit-file edn-filename content)
        (build-code-formatter/format-file edn-filename header)
        content
        (catch Exception e
          (throw (ex-info "Impossible to update the .edn file"
                          {:deps-edn-filename edn-filename,
                           :exception e,
                           :content content})))))
  ([deps-edn-filename content] (spit-edn deps-edn-filename content nil)))

(defn create-tmp-edn
  "Create a temporary file with edn extension
  Params:
  * `filename` relative filename to save"
  [filename]
  (-> (build-files/create-temp-dir)
      (build-files/create-file-path filename)))
