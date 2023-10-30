(ns automaton-build-app.tasks.reports-test
  (:require [automaton-build-app.tasks.reports :as sut]
            [automaton-build-app.app :as build-app]
            [automaton-build-app.file-repo.clj-code :as build-clj-code]))

(comment
  (def build-data
    (-> ""
        (@build-app/build-app-data_)))
  (def clj-repo
    (-> build-data
        build-app/src-dirs
        build-clj-code/make-clj-repo-from-dirs))
  (sut/keyword-report [build-data clj-repo])
  ;
)
