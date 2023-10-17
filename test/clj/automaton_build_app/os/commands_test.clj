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

(deftest first-cmd-failing-test
  (testing "Failing command is found"
    (is (= 3
           (sut/first-cmd-failing [[0 ""]
                                   [0 ""]
                                   [0 ""]
                                   [1 ""]])))
    (is (= 1
           (sut/first-cmd-failing [[0 ""]
                                   [1 ""]
                                   [0 ""]
                                   [1 ""]])))))
