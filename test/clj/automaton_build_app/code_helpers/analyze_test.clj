(ns automaton-build-app.code-helpers.analyze-test
  "The strings are exploded by character so the search won't find that word in this namespace"
  (:require
   [automaton-build-app.code-helpers.analyze :as sut]
   [automaton-build-app.code-helpers.analyze.reports :as build-clj-code-reports]
   [automaton-build-app.file-repo.clj-code :as build-clj-code]
   [clojure.test :refer [deftest is testing]]))

(deftest execute-report-test
  (testing "Report on empty data is ok"
    (is (-> (sut/execute-report (build-clj-code/->CljCodeFilesRepository {})
                                ::build-clj-code-reports/comments)
            sut/report-empty?)))

  (testing "Css report is returning expected lines"
    (is (-> (sut/execute-report (build-clj-code/->CljCodeFilesRepository {"foo.clj" [":class \":a"]})
                                ::build-clj-code-reports/css)
            sut/report-empty?
            not)))
  (testing "Css report is returneing expected lines"
    (is (-> (sut/execute-report (build-clj-code/->CljCodeFilesRepository {"automaton-build.test-toolings" {"btt" ["test-toolings-test.clj"]
                                                                                                           "bt" ["foo.clj"]}})
                                ::build-clj-code-reports/one-alias-per-ns))))
  (testing "Css report is returneing expected lines"
    (is (-> (sut/execute-report (build-clj-code/->CljCodeFilesRepository {"btt" {"automaton-build.test-toolings" ["test-toolings-test.clj"]
                                                                                 "automaton-build.test-test" ["foo.clj"]},
                                                                          "bc" {"automaton-build.core" ["test-toolings-test.clj" "foo.clj"]},
                                                                          "bt" {"automaton-build.test-toolings" ["foo.clj"]}})
                                ::build-clj-code-reports/alias)))))
