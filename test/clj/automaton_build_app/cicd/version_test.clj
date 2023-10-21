(ns automaton-build-app.cicd.version-test
  (:require
   [automaton-build-app.cicd.version :as sut]
   [clojure.test :refer [deftest is testing]]))

(deftest version-to-push-test
  (testing "Version to push returns a string"
    (is (string?
         (sut/version-to-push "target" "1.3.%d")))))
