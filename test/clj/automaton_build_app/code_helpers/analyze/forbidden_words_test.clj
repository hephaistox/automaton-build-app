(ns automaton-build-app.code-helpers.analyze.forbidden-words-test
  (:require [automaton-build-app.code-helpers.analyze.forbidden-words :as sut]
            [automaton-build-app.file-repo.clj-code :as build-clj-code]
            [clojure.test :refer [deftest is testing]]))

(deftest execute-report-test
  (let [clj-repo (build-clj-code/->CljCodeFileRepo {"foo.clj" ["  automaton-foobar"]
                                                    "foo2.clj" ["  automaton_foobar"]})]
    (testing "Comment report is returning expected lines"
      (is (= [["foo.clj" ["automaton-foobar"]] ["foo2.clj" ["automaton_foobar"]]]
             (sut/forbidden-words-matches clj-repo (sut/coll-to-alternate-in-regexp [#"automaton[-_]foobar" #"automaton[-_]foobar"])))))))

(deftest coll-to-alternate-in-regexp-test
  (testing "One word is transformed"
    (is (= "(automaton[-_]foobar|automaton[-_]foobar)"
           (str (sut/coll-to-alternate-in-regexp [#"automaton[-_]foobar" #"automaton[-_]foobar"]))))))
