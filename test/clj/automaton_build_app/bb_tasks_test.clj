(ns automaton-build-app.bb-tasks-test
  (:require [automaton-build-app.bb-tasks :as sut]
            [clojure.test :refer [deftest is testing]]))

(deftest update-bb-task-test
  (testing "Each bb-task in registry is ok"
    (let [blog-task (-> sut/registry
                        (select-keys ['blog])
                        vals
                        first)]
      (is (= {:doc "Regenerate blog documents and pages",
              :task (list 'execute-task
                          ''automaton-build-app.tasks.doc-clj/cicd-doc
                          {:executing-pf :clj})}
             (sut/update-bb-task blog-task))))))

(deftest add-bb-tasks-test
  (testing "Check all tasks are added"
    (is (= (count sut/registry)
           (->> {}
                (sut/add-bb-tasks sut/registry)
                count)))))

(comment
  (sut/update-bb-tasks "" {'foo {:doc "", :tasks :init}} #{'foo})
  ;
)
