(ns automaton-build-app.schema-test
  (:require [automaton-build-app.schema :as sut]
            [clojure.test :refer [deftest is testing]]))

(deftest schema
  (testing "Valid schema"
    (is (sut/valid? [:tuple :string :int]
                          ["hey" 12])))
  (testing "Invalid schema, throws an exception"
    (is (not (sut/valid? [:tuple :string :int]
                               [12 12])))))
