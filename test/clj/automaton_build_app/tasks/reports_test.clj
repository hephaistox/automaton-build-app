(ns automaton-build-app.tasks.reports-test
  (:require [automaton-build-app.tasks.reports :as sut]
            [automaton-build-app.app :as build-app]
            [automaton-build-app.file-repo.clj-code :as build-clj-code]))

(comment
  (let [app (build-app/build "")
        clj-repo (-> app
                     build-app/src-dirs
                     build-clj-code/make-clj-repo-from-dirs)]
    (sut/keyword-report [app clj-repo]))
  ;
)
