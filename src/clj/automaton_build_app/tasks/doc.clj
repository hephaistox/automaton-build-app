(ns automaton-build-app.tasks.doc
  "All the functions related to blogging/creating content"
  (:require
   [automaton-build-app.doc.blog :as build-blog]
   [automaton-build-app.code-helpers.code-stats :as build-code-stats]
   [automaton-build-app.doc.code-doc :as app-code-doc]
   [automaton-build-app.code-helpers.test-toolings :as build-test-toolings]
   [automaton-build-app.doc.mermaid :as build-mermaid]
   [automaton-build-app.app :as build-app]))

(defn blog-task
  "Blog task"
  [& _opts]
  (let [{:keys [customer-materials]} (build-app/build-app-data "")
        {:keys [dir html-dir pdf-dir]} customer-materials]
    (build-blog/blog-process dir html-dir pdf-dir)))

(defn code-doc
  "Create the code documentation"
  [& _opts]
  (let [{:keys [app-name doc] :as app-data} (build-app/build-app-data "")
        {:keys [code-doc]} doc
        {:keys [title description dir]} code-doc
        app-dirs (-> app-data
                     build-app/src-dirs)]
    (app-code-doc/build-doc "" app-name app-dirs title description dir)))

(defn mermaid
  "Build all mermaid files"
  [& _opts]
  (let [{:keys [doc]} (build-app/build-app-data "")
        {:keys [archi]} doc
        {:keys [dir]} archi]
    (build-mermaid/build-all-files dir)))

(defn code-stats
  [& _opts]
  (let [filename (get-in (build-app/build-app-data "")
                         [:doc :code-stats :output-file])]
    (->> (build-code-stats/line-numbers "")
         (build-code-stats/stats-to-md filename))))

(defn reports
  [& _opts]
  (build-test-toolings/execute-report ""
                                   ::build-test-toolings/comments))

(defn cicd-doc
  "Generate all docs for cicd"
  [opts]
  ((juxt code-doc blog-task mermaid code-stats reports)
   opts))
