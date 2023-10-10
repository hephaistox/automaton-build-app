(ns automaton-build-app.configuration.edn-read-test
  (:require
   [automaton-build-app.configuration.edn-read :as sut]
   [clojure.test :refer [deftest is testing]]
   [clojure.java.io :as io]))

(deftest read-edn-test
  (testing "Find the test configuration stub"
    (is (= {:foo "bar"
            :bar 10}
           (sut/read-edn (io/resource "configuration/stub.edn"))))))
