(ns automaton-build-app.doc.code-doc
  "Code documentation creation
  Proxy to codox"
  (:require
   [automaton-build-app.apps.app :as build-app]
   [automaton-build-app.code-helpers.build-config :as build-build-conf]
   [automaton-build-app.log :as log]
   [automaton-build-app.os.files :as files]
   [codox.main :as codox]))

(defn doc-subdir
  "The directory of the documentation
  Params:
  * `app-dir` the root directory of the app"
  [app-dir]
  (files/create-dir-path app-dir (build-build-conf/read-param [:doc :code-doc :dir]
                                                              "docs/code")))

(defn build-doc
  "Generate the documentation
  Params:
  * `opts` for compatibility with -X"
  [_opts]
  (let [doc-title (build-build-conf/read-param [:doc :code-doc :title]
                                               "Application title")
        doc-description (build-build-conf/read-param [:doc :code-doc :description]
                                                     "Application description")
        app-dir ""
        app-dirs (-> (build-app/build-app-data app-dir)
                     build-app/get-clj-c-s-src-dirs)]
    (log/info-format "Build application documentation cust-app `%s` in directory `%s`"
                     doc-title
                     (build-build-conf/read-param [:app-name]
                                                  "Application name"))
    (let [dir (doc-subdir app-dir)]
      (files/create-dirs dir)
      (codox/generate-docs {:name doc-title
                            :version "1.0"
                            :source-paths app-dirs
                            :output-path dir
                            :description doc-description}))))
