(ns automaton-build-app.tasks.doc-clj
  "All the functions related to blogging/creating content"
  (:require [automaton-build-app.app :as build-app]
            [automaton-build-app.doc.blog :as build-blog]
            [automaton-build-app.doc.code-doc :as app-code-doc]
            [automaton-build-app.os.exit-codes :as build-exit-codes]
            [automaton-build-app.doc.mermaid :as build-mermaid]
            [automaton-build-app.log :as build-log]))

(defn blog-task
  "Blog task"
  [{:keys [min-level], :as _parsed-cli-opts}]
  (build-log/set-min-level! min-level)
  (let [{:keys [customer-materials]} (@build-app/build-app-data_ "")
        {:keys [dir html-dir pdf-dir]} customer-materials]
    (build-blog/blog-process dir html-dir pdf-dir)))

(defn code-doc
  "Create the code documentation"
  [{:keys [min-level], :as _parsed-cli-opts}]
  (build-log/set-min-level! min-level)
  (let [{:keys [_app-name doc], :as app-data} (@build-app/build-app-data_ "")
        {:keys [_code-doc reports]} doc
        ;{:keys [title description dir]} code-doc
        _app-dirs (-> app-data
                      build-app/src-dirs)
        res (and (app-code-doc/vizualize-ns reports)
                 (app-code-doc/vizualize-deps reports)
                 ;; codox fails
                 #_(app-code-doc/build-doc ""
                                           app-name
                                           app-dirs
                                           title
                                           description
                                           dir))]
    (when-not res (System/exit build-exit-codes/catch-all))
    res))

(defn mermaid
  "Build all mermaid files"
  [{:keys [min-level], :as _parsed-cli-opts}]
  (build-log/set-min-level! min-level)
  (let [{:keys [doc]} (@build-app/build-app-data_ "")
        {:keys [archi]} doc
        {:keys [dir]} archi]
    (build-mermaid/build-all-files dir)))

(defn cicd-doc
  "Generate all docs for cicd"
  [parsed-cli-opts]
  ((juxt code-doc blog-task mermaid) parsed-cli-opts))
