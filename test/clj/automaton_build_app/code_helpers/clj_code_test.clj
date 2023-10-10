(ns automaton-build-app.code-helpers.clj-code-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [automaton-build-app.code-helpers.clj-code :as sut]))

(deftest search-filenames-test
  (testing "Some clojure files are found in the current project"
    (is (< 0
           (-> (sut/search-clj-filenames ".")
               count)))))

(deftest is-clj-file-test
  (testing "Compatible files are found"
    (is (sut/is-clj-file "foo.clj"))
    (is (sut/is-clj-file "foo.cljc"))
    (is (sut/is-clj-file "foo.cljs")))
  (testing "Incompatible files are found"
    (is (not (sut/is-clj-file "foo.clj ")))
    (is (not (sut/is-clj-file "foo.cljcaze")))
    (is (not (sut/is-clj-file "foo")))
    (is (not (sut/is-clj-file "")))
    (is (not (sut/is-clj-file nil)))))
