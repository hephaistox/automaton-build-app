(ns automaton-build-app.doc.markdown
  "Markdown adapter"
  (:require
   [markdown.core :as markdown]))

(defn md-to-html
  "Transform md file to html"
  [md-filename html-filename]
  (markdown/md-to-html md-filename
                       html-filename))
