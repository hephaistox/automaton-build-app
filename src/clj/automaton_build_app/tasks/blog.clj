(ns automaton-build-app.tasks.blog
  (:require [automaton-build-app.doc.blog :as build-blog]
            [automaton-build-app.log :as build-log]))

(defn blog
  [_cli-opts app _bb-edn-args]
  (let [customer-materials (get-in app [:build-config :customer-materials])
        {:keys [dir html-dir pdf-dir]} customer-materials]
    (if (nil? customer-materials)
      (do (build-log/debug "Blog is skipped as no parameters are found") true)
      (build-blog/blog-process dir html-dir pdf-dir))
    true))
