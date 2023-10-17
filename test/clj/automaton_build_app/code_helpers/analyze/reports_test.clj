(ns automaton-build-app.code-helpers.analyze.reports-test
  (:require
   [automaton-build-app.code-helpers.analyze.reports :as sut]
   [clojure.test :refer [deftest is testing]]))

(def css-pattern
  (get-in sut/reports
          [::sut/css :pattern]))

(deftest css-pattern-test
  (testing "Detect class string"
    (is (not (nil?
              (sut/search-line css-pattern
                               "This :class \"should be discovered"))))
    (is (not (nil?
              (sut/search-line css-pattern
                               "This :class     \"should be discovered"))))
    (is (nil? (sut/search-line css-pattern
                               "This :class should not be discovered"))))
  (testing "Class vectors are expected whatever the form"
    (is (nil?
         (sut/search-line css-pattern
                          "This :class (apply vec should be discovered")))
    (is (nil?
         (sut/search-line css-pattern
                          "This :class (apply \nvec should be discovered")))
    (is (nil?
         (sut/search-line css-pattern
                          "This :class (vec should be discovered")))
    (is (nil?
         (sut/search-line css-pattern
                          "This :class\n(\nvector\n should be discovered"))))
  (testing "Accept class litteral vectors"
    (is (nil? (sut/search-line css-pattern
                               "This :class [... should be accepted"))))
  (testing "Dectect class on html element"
    (is (not (nil?
              (sut/search-line css-pattern
                               "This :a#id should be discovered")))))
  (testing "Dectect class id on html element"
    (is (not (nil?
              (sut/search-line css-pattern
                               "This :a.foo should be discovered"))))
    (is (not (nil?
              (sut/search-line css-pattern
                               "This :a#foo should be discovered"))))
    (is (nil?
         (sut/search-line css-pattern
                          "This :annn.foo should not be discovered")))
    (is (nil?
         (sut/search-line css-pattern
                          "This .foo should not be discovered")))
    (is (nil?
         (sut/search-line css-pattern
                          "This #foo should not be discovered")))))

(def alias-pattern
  (get-in sut/reports
          [::sut/alias :pattern]))

(deftest search-aliases-in-line-test
  (testing "Variants without aliases"
    (is (nil?
         (sut/search-line alias-pattern "[]"))))
  (testing "Variants with aliases"
    (is (= ["[ff :as tt]" "ff" "tt" nil]
           (sut/search-line alias-pattern "[ff :as tt]")))
    (is (= ["[Ff :as tt]" "Ff" "tt" nil]
           (sut/search-line alias-pattern "[Ff :as tt]")))
    (is (= ["[f0f :as tt]" "f0f" "tt" nil]
           (sut/search-line alias-pattern "[f0f :as tt]"))))
  (testing "Variants with complex names aliases"
    (is (= ["[f*0 :as tt]" "f*0" "tt" nil]
           (sut/search-line alias-pattern "[f*0 :as tt]")))
    (is (= ["[f+f :as tt]" "f+f" "tt" nil]
           (sut/search-line alias-pattern "[f+f :as tt]")))
    (is (= ["[f!f :as tt]" "f!f" "tt" nil]
           (sut/search-line alias-pattern "[f!f :as tt]")))
    (is (= ["[f-f :as tt]" "f-f" "tt" nil]
           (sut/search-line alias-pattern "[f-f :as tt]")))
    (is (= ["[f_f :as tt]" "f_f" "tt" nil]
           (sut/search-line alias-pattern "[f_f :as tt]")))
    (is (= ["[f'f :as tt]" "f'f" "tt" nil]
           (sut/search-line alias-pattern "[f'f :as tt]")))
    (is (= ["[f?f :as tt]" "f?f" "tt" nil]
           (sut/search-line alias-pattern "[f?f :as tt]")))
    (is (= ["[f<f :as tt]" "f<f" "tt" nil]
           (sut/search-line alias-pattern "[f<f :as tt]")))
    (is (= ["[f>f :as tt]" "f>f" "tt" nil]
           (sut/search-line alias-pattern "[f>f :as tt]")))
    (is (= ["[f=f :as tt]" "f=f" "tt" nil]
           (sut/search-line alias-pattern "[f=f :as tt]")))
    (is (= ["[f=f :refer [tt uu]]" "f=f" nil ":refer"]
           (sut/search-line alias-pattern "[f=f :refer [tt uu]]")))
    (is (= ["[fF9*+!-'?<>=f :as tt]" "fF9*+!-'?<>=f" "tt" nil]
           (sut/search-line alias-pattern "[fF9*+!-'?<>=f :as tt]")))
    (is (= ["[f-f :as fF9*+!-'?<>=f]" "f-f" "fF9*+!-'?<>=f" nil]
           (sut/search-line alias-pattern "[f-f :as fF9*+!-'?<>=f]"))))
  (testing "Spaces are caught"
    (is (= ["  [  ff   :as   tt ]  " "ff" "tt" nil]
           (sut/search-line alias-pattern "  [  ff   :as   tt ]  ")))
    (is (= ["   [automaton-core.log :as log]" "automaton-core.log" "log" nil]
           (sut/search-line alias-pattern "   [automaton-core.log :as log]")))))


(def output-files
  {::sut/comments "comment-tmp-file.md"})

(def code-files-repo-stub
  {"test-toolings-test.clj" ["(ns automaton-buildtest-toolings-test"
                             "(:require"
                             "[clojure.test :refer [deftest testing is]]"
                             ""
                             "[automaton-build.test-toolings :as btt]))"
                             "[automaton-build.core :as bc]))"
                             ""]
   "foo.clj" ["(ns foo"
              "(:require"
              "[clojure.set :refer [union]]"
              "[automaton-build.test-toolings :as bt]))"
              "[automaton-build.test-test :as btt]))"
              ""
              (format "This is ;;%s \n;;  \n  %s ;; %s ;; %s" T D N F)
              "[automaton-build.core :as bc]))"
              ""]
   "foo-test.clj" ["(ns foo-test"
                   "(:require"
                   "[clojure.test :refer [deftest is testing]]"
                   ""
                   "[foo :as sut]))"
                   ""]})
