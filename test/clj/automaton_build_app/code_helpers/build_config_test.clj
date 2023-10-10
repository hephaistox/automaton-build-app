(ns automaton-build-app.code-helpers.build-config-test
  (:require
   [automaton-build-app.code-helpers.build-config :as sut]
   [clojure.test :refer [deftest is testing]]))

(deftest search-for-build-config-test
  (testing "At least current project should be found"
    (is (> (count (sut/search-for-build-config))
           0))))
