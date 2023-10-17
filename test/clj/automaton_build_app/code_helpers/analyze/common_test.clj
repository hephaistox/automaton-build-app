(ns automaton-build-app.code-helpers.analyze.common-test
  (:require
   [automaton-build-app.code-helpers.analyze.common :as sut]
   [clojure.test :refer [deftest is testing]]))

(deftest search-line
  (testing "Search line"
    (is (nil? (sut/search-line #"ff"
                               "This text is not")))
    (is (= "ff"
           (sut/search-line #"ff"
                            "This text is containing ff !")))))
