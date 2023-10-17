(ns automaton-build-app.utils.namespace-test
  (:require
   [automaton-build-app.utils.namespace :as sut]
   [clojure.test :refer [deftest is testing]]))

(deftest update-last-test
  (testing "Add clj to last element"
    (is (= ["aze" "foo.clj"]
           (sut/update-last "aze" "foo"))))
  (testing "Add clj to last element"
    (is (nil?
         (sut/update-last)))))

(deftest ns-to-file-test
  (testing "Is ok"
    (is (= "automaton_build_app/utils/namespace_test.clj"
           (sut/ns-to-file 'automaton-build-app.utils.namespace-test)))))
