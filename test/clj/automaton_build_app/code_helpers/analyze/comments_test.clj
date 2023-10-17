(ns automaton-build-app.code-helpers.analyze.comments-test
  (:require
   [automaton-build-app.code-helpers.analyze.comments :as sut]
   [automaton-build-app.code-helpers.analyze.common :as build-analyze-common]
   [automaton-build-app.file-repo.clj-code :as build-clj-code]
   [clojure.test :refer [deftest is testing]]))

(deftest comment-pattern-test
  (testing "T O D O s are found (space intentionally left so rg won't find the word)"
    (is (some? (build-analyze-common/search-line sut/regexp (format "  ;;%s  " sut/T))))
    (is (some? (build-analyze-common/search-line sut/regexp (format ";;%s " sut/T))))
    (is (some? (build-analyze-common/search-line sut/regexp (format "  ;;       %s " sut/T))))
    (is (some? (build-analyze-common/search-line sut/regexp (format ";;%s This is a to do" sut/T)))))
  (testing "Check other words"
    (is (some? (build-analyze-common/search-line sut/regexp (format "  ;;%s  " sut/N))))
    (is (some? (build-analyze-common/search-line sut/regexp (format "  ;;%s " sut/D))))
    (is (some? (build-analyze-common/search-line sut/regexp (format "  ;;%s " sut/F))))))

(deftest execute-report-test
  (testing "Comment report is returning expected lines"
    (is (-> (sut/execute-report (build-clj-code/->CljCodeFilesRepository {"foo.clj" [";;TODO foo is bar"]}))
            sut/is-empty?
            not))))
