(ns automaton-build-app.tasks.code-doc
  (:require [automaton-build-app.app :as build-app]
            [automaton-build-app.doc.blog :as build-blog]
            [automaton-build-app.doc.code-doc :as app-code-doc]
            [automaton-build-app.os.exit-codes :as build-exit-codes]
            [automaton-build-app.doc.mermaid :as build-mermaid]
            [automaton-build-app.log :as build-log]))

(defn- vizualize-ns
  [app-dir]
  (-> (@build-app/build-app-data_ app-dir)
      (get-in [:doc :reports :output-files :deps-ns] "docs/code/deps-ns.svg")
      app-code-doc/vizualize-ns))

(defn- vizualize-deps
  [app-dir]
  (-> (@build-app/build-app-data_ app-dir)
      (get-in [:doc :reports :output-files :deps] "docs/code/deps.svg")
      app-code-doc/vizualize-deps))

(defn- doc-string
  [app-dir]
  (let [{:keys [app-name]
         :as app-data}
        (@build-app/build-app-data_ app-dir)
        code-doc (get-in app-data [:doc :code-doc] {})
        {:keys [title description dir]} code-doc
        app-dirs (build-app/src-dirs app-data)]
    (if (empty? code-doc)
      (do (build-log/debug "doc string doc generation is skipped as no parameters are found") true)
      (app-code-doc/build-doc app-dir app-name app-dirs title description dir))))

(defn- blog-task
  [app-dir]
  (let [{:keys [customer-materials]} (@build-app/build-app-data_ app-dir)
        {:keys [dir html-dir pdf-dir]} customer-materials]
    (if (nil? customer-materials)
      (do (build-log/debug "Blog is skipped as no parameters are found") true)
      (build-blog/blog-process dir html-dir pdf-dir))
    true))

(defn- mermaid
  [app-dir]
  (-> (@build-app/build-app-data_ app-dir)
      (get-in [:doc :archi :dir] "doc/archi/dir")
      build-mermaid/build-all-files))

(defn code-doc
  "Generate the code documentation"
  [{:keys [min-level]
    :as _parsed-cli-opts}]
  (let [app-dir ""]
    (build-log/set-min-level! min-level)
    (when-not ((juxt vizualize-ns vizualize-deps doc-string blog-task mermaid) app-dir) (System/exit build-exit-codes/catch-all))))
