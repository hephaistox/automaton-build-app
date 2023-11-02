(ns automaton-build-app.bb-tasks-test
  (:require [automaton-build-app.bb-tasks :as sut]
            [automaton-build-app.code-helpers.bb-edn :as build-bb-edn]
            [clojure.test :refer [deftest is testing]]))

(deftest update-bb-task-test
  (testing "Each bb-task in registry is ok"
    (let [task-name (first (keys sut/registry))
          blog-task (-> sut/registry
                        (select-keys [(symbol task-name)])
                        vals
                        first)]
      (is (map? (sut/update-bb-task blog-task))))))

(deftest add-bb-tasks-test
  (testing "Check all tasks are added"
    (is (= (count sut/registry)
           (->> {}
                (sut/add-bb-tasks sut/registry)
                count)))))

(deftest remove-bb-tasks-test
  (let [task-name (first (keys sut/registry))]
    (testing "Remove the bb tasks"
      (is (not (-> (sut/remove-bb-tasks #{task-name} sut/registry)
                   keys
                   set
                   (contains? task-name)))))
    (testing "Non removed task is found"
      (is (-> sut/registry
              keys
              set
              (contains? (symbol task-name)))))))

(deftest update-bb-tasks*-test
  (let [task-name (-> sut/registry
                      keys
                      first)]
    (testing "Check if updating is ok"
      (is (not (-> (sut/update-bb-tasks* sut/registry #{task-name} (build-bb-edn/read-bb-edn ""))
                   :tasks
                   keys
                   set
                   (contains? (symbol task-name)))))
      (is (-> (sut/update-bb-tasks* sut/registry #{(str task-name "-different-task")} (build-bb-edn/read-bb-edn ""))
              :tasks
              keys
              set
              (contains? (symbol task-name)))))))

(comment
  (sut/update-bb-tasks ""
                       {'foo {:doc ""
                              :tasks :init}}
                       #{"foo"})
  ;
)
