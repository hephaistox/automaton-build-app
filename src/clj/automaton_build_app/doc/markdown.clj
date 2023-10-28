(ns automaton-build-app.doc.markdown
  "Markdown adapter"
  (:require [automaton-build-app.os.files :as build-files]
            [clojure.string :as str]
            [markdown.core :as markdown]))

(defn md-to-html
  "Transform md file to html"
  [md-filename html-filename]
  (markdown/md-to-html md-filename html-filename))

(defn create-md
  "Build the markdown file
  Params:
  * `filename` is the name of the md file to store`
  * `content` is the string to spit"
  [filename content]
  (let [formatted-content
          (if (sequential? content) (str/join "\n" content) (str content))]
    (build-files/spit-file filename formatted-content)))
