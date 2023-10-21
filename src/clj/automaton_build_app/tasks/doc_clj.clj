(ns automaton-build-app.tasks.doc-clj
  "All the functions related to blogging/creating content"
  (:require [automaton-build-app.app :as build-app]
            [automaton-build-app.doc.blog :as build-blog]
            [automaton-build-app.doc.code-doc :as app-code-doc]
            [automaton-build-app.doc.mermaid :as build-mermaid]
            [automaton-build-app.log :as build-log]
            [clojure.tools.deps.graph :as tools-deps-graph]
            [io.dominic.vizns.core :as vizns]))

(defn blog-task
  "Blog task"
  [{:keys [min-level], :as _opts}]
  (build-log/set-min-level! min-level)
  (let [{:keys [customer-materials]} (@build-app/build-app-data_ "")
        {:keys [dir html-dir pdf-dir]} customer-materials]
    (build-blog/blog-process dir html-dir pdf-dir)))

(defn code-doc
  "Create the code documentation"
  [{:keys [min-level], :as _opts}]
  (build-log/set-min-level! min-level)
  (let [{:keys [app-name doc], :as app-data} (@build-app/build-app-data_ "")
        {:keys [code-doc reports]} doc
        {:keys [title description dir]} code-doc
        app-dirs (-> app-data
                     build-app/src-dirs)]
    (build-log/info "Vizualization of ns - deps link")
    (vizns/-main "single"
                 "-o" (get-in reports
                              [:output-files :deps-ns]
                              "docs/code/deps-ns.svg")
                 "-f" "svg")
    (build-log/info "Graph of dependencies")
    (tools-deps-graph/graph
      {:output (get-in reports [:output-files :ns] "docs/code/ns.svg")})
    (build-log/info "Code documetation")
    (app-code-doc/build-doc "" app-name app-dirs title description dir)))

(defn mermaid
  "Build all mermaid files"
  [{:keys [min-level], :as _opts}]
  (build-log/set-min-level! min-level)
  (let [{:keys [doc]} (@build-app/build-app-data_ "")
        {:keys [archi]} doc
        {:keys [dir]} archi]
    (build-mermaid/build-all-files dir)))

(defn cicd-doc
  "Generate all docs for cicd"
  [opts]
  ((juxt code-doc blog-task mermaid) opts))
