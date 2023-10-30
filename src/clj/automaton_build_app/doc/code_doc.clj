(ns automaton-build-app.doc.code-doc
  "Code documentation creation
  Proxy to codox"
  (:require [automaton-build-app.log :as build-log]
            [automaton-build-app.os.files :as build-files]
            [io.dominic.vizns.core :as vizns]
            [clojure.tools.deps.graph :as tools-deps-graph]
            [codox.main :as codox]))

(defn build-doc
  "Generate the documentation
  Params:
  * `doc-title` title of the document
  * `doc-description` decscription of the document"
  [app-dir app-name app-dirs doc-title doc-description doc-dir]
  (build-log/info "Code documentation")
  (build-log/trace-map "Build doc with parameters"
                       :app-dir app-dir
                       :app-name app-name
                       :doc-title doc-title
                       :doc-description doc-description
                       :doc-dir doc-dir)
  (build-log/trace-data app-dirs "Directories search for code are")
  (build-log/info-format
    "Build application documentation cust-app `%s` in directory `%s`"
    doc-title
    app-name)
  (let [dir (build-files/create-dir-path app-dir doc-dir)]
    (build-files/create-dirs dir)
    (codox/generate-docs {:name doc-title,
                          :version "1.0",
                          :source-paths app-dirs,
                          :namespaces [#"clj[s|c]?$"],
                          :output-path dir,
                          :description doc-description})
    true))

(defn vizualize-ns
  "Vizualise all namespaces relations"
  [deps-filename]
  (build-log/info "Graph of ns - deps link")
  (build-log/trace-format "Graph stored in `%s`" deps-filename)
  (build-files/create-parent-dirs deps-filename)
  (vizns/-main "single" "-o" deps-filename "-f" "svg")
  true)

(defn vizualize-deps
  "Vizualize the dependencies between deps"
  [output-filename]
  (build-log/info "Graph of deps")
  (build-log/trace-format "Graph stored in `%s`" output-filename)
  (build-files/create-parent-dirs output-filename)
  (tools-deps-graph/graph {:output output-filename})
  true)
