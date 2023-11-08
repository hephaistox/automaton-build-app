(ns automaton-build-app.tasks.registry.find-test
  (:require [automaton-build-app.tasks.registry.find :as sut]
            [clojure.test :refer [deftest is testing]]))

(def stub
  [['clean
    {:doc "Clean cache files for compiles, and logs"
     :name :clean
     :la-test {:cmd ["bb" "clean"]}
     :task-fn 'automaton-build-app.tasks.clean/clean}]
   ['clean-hard
    {:doc
     "Clean all files which are not under version control (it doesn't remove untracked file or staged files if there are eligible to `git add .`)"
     :la-test {:cmd ["bb" "clean-hard"]
               :process-opts {:in "q"}}
     :name :clean-hard
     :pf :clj
     :task-fn 'automaton-build-app.tasks.clean-hard/clean-hard}]])

(deftest task-selection-test
  (testing "An existing symbol is found"
    (is (= :clean
           (-> (sut/task-selection stub ['clean])
               first
               :name))))
  (testing "Registry is seen as a map"
    (is (= :clean
           (-> (sut/task-selection stub ['clean])
               first
               :name))))
  (testing "Not found symbol" (is (empty? (sut/task-selection stub ['not-existing-task])))))
