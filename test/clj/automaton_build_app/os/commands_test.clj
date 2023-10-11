(ns automaton-build-app.os.commands-test
  (:require
   [automaton-build-app.os.commands :as sut]
   [clojure.test :refer [deftest is testing]]))

(deftest execute-and-trace-test
  (testing "Silently executing pwd is sucessfull"
    (is (sut/execute-and-trace ["pwd"]))
    (is (sut/execute-and-trace ["pwd"]
                               ["pwd"])))
  (testing "failing command are detected"
    (is (not (sut/execute-and-trace ["pwd"]
                                    ["this-command-does-not-exist"]
                                    ["pwd"])))))

(deftest execute-silently-test
  (testing "Silently executing pwd is sucessfull"
    (is (sut/execute-silently ["pwd"]))
    (is (sut/execute-silently ["pwd"]
                              ["pwd"])))
  (testing "Silently executing a non existing command is failing"
    (is (not (sut/execute-silently ["this-command-does-not-exist"])))))

(deftest execute-get-string-test
  (testing "Get string actually return the strings"
    (is (every? string? (sut/execute-get-string ["pwd"])))))
