(ns automaton-build-app.tasks.blog
  (:require [automaton-build-app.doc.blog :as build-blog]
            [automaton-build-app.os.exit-codes :as build-exit-codes]))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn exec
  [_task-map app_data]
  (let [{:keys [dir html-dir pdf-dir]} app_data]
    (if (true? (build-blog/blog-process dir html-dir pdf-dir)) build-exit-codes/ok build-exit-codes/catch-all)))
