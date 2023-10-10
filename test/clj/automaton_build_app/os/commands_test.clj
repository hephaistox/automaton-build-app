(ns automaton-build-app.os.commands-test
  (:require
   [automaton-build-app.os.commands :as sut]
   [clojure.test :refer [deftest is testing]]))

(deftest execute-test
  (testing "Check execute with :out :string is ok"
    (is (= [[0 "foo\n"]]
           (sut/execute ["echo" "foo" {:out :string}]))))
  (testing "Error is caught and next command is tried"
    (is (= [[-1 "Unexpected error during execution of this command[\"pwd2\" {:out :string}]"]
            [0 "coucou\n"]]
           (sut/execute ["pwd2" {:out :string}]
                        ["echo" "coucou" {:out :string}])))))

(comment
  (sut/execute ["pwd" {:out :string}])
  (sut/execute ["pwd2" {:out :string}])
  ;
  )

