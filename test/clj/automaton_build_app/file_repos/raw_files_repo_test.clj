(ns automaton-build-app.file-repos.raw-files-repo-test
  (:require
   [automaton-build-app.file-repos.raw-files-repos :as sut]
   [clojure.test :refer [deftest is testing]]))

(def files-repo {"foo.clj"  ["This is"
                             " the foo file"
                             " hey!"]
                 "foo.edn" ["test"]
                 "bar.cljc" ["This is the bar file"
                             " ho ho!"]})

(deftest exclude-files-test
  (testing "Exclude none file"
    (is (= files-repo
           (sut/exclude-files files-repo
                              #{"none"}))))
  (testing "Exlude all files"
    (is (= {}
           (sut/exclude-files files-repo
                              #{"foo.clj" "bar.cljc" "foo.edn"})))))
