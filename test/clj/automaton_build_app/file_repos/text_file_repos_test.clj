(ns automaton-build-app.file-repos.text-file-repos-test
  (:require
   [automaton-build-app.file-repos.text-file-repos :as sut]
   [clojure.test :refer [deftest is testing]]))

(deftest load-repo-test
  (testing "No file is skipped"
    (is (= 2
           (-> (sut/load-repo ["build_config.edn"
                               "bb.edn"])
               count)))))
(comment
  (sut/load-repo ["README.md"
                  "bb.edn"])
  ;
  )
