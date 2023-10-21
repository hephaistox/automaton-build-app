(ns automaton-build-app.os.commands-test
  (:require
   [automaton-build-app.os.commands :as sut]
   [clojure.test :refer [deftest is testing]]))

(deftest execute-with-exit-code-test
  (testing "A simple command return"
    (is (= [[0 "foo\n"]]
           (sut/execute-with-exit-code ["echo" "foo"])))
    (is (= [[0 "foo\n"]
            [0 "bar\n"]]
           (sut/execute-with-exit-code ["echo" "foo"]
                                       ["echo" "bar"])))
    (is (= [0 -1]
           (mapv first
                 (sut/execute-with-exit-code ["echo" "foo"]
                                             ["arg"]))))))

(deftest execute-and-trace-test
  (testing "Executing pwd is sucessfull"
    (is (sut/execute-and-trace ["pwd" {:out :string}]))
    (is (sut/execute-and-trace ["pwd" {:out :string}]
                               ["pwd" {:out :string}])))
  (testing "failing command are detected"
    (is (not (sut/execute-and-trace ["this-command-does-not-exist"])))
    (is (not (sut/execute-and-trace ["pwd" {:out :string}]
                                    ["this-command-does-not-exist"]
                                    ["pwd" {:out :string}])))))

(deftest execute-get-string-test
  (testing "Get string actually return the strings"
    (is (= ["foo\n" "bar\n"]
           (sut/execute-get-string ["echo" "foo"]
                                   ["echo" "bar"])))))

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
                                   [1 ""]]))))
  (testing "Successful command is ok"
    (is (nil?
         (sut/first-cmd-failing [[0 ""]
                                 [0 ""]
                                 [0 ""]
                                 [0 ""]])))))
