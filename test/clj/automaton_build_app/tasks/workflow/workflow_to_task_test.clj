(ns automaton-build-app.tasks.workflow.workflow-to-task-test
  (:require [automaton-build-app.tasks.workflow.workflow-to-task :as sut]
            [clojure.test :refer [deftest is testing]]))

(deftest update-registry-workflow-entries-test
  (testing "Non workflow tasks are not modified"
    (let
      [registry-stub
       [['clean
         {:doc "Clean cache files for compiles, and logs"
          :la-test {:cmd ["bb" "clean"]}
          :task-fn 'automaton-build-app.tasks.clean/clean}]
        ['clean-hard
         {:doc
          "Clean all files which are not under version control (it doesn't remove untracked file or staged files if there are eligible to `git add .`)"
          :la-test {:cmd ["bb" "clean-hard"]
                    :process-opts {:in "q"}}
          :task-fn 'automaton-build-app.tasks.clean-hard/clean-hard}]]]
      (is (= registry-stub (sut/update-registry-workflow-entries registry-stub)))))
  (testing "Workflow tasks"
    (let
      [registry-stub
       [['clean
         {:doc "Clean cache files for compiles, and logs"
          :la-test {:cmd ["bb" "clean"]}
          :task-fn 'automaton-build-app.tasks.clean/clean}]
        ['clean-hard
         {:doc
          "Clean all files which are not under version control (it doesn't remove untracked file or staged files if there are eligible to `git add .`)"
          :la-test {:cmd ["bb" "clean-hard"]
                    :process-opts {:in "q"}}
          :task-fn 'automaton-build-app.tasks.clean-hard/clean-hard}]
        ['wk-test
         {:doc "for test"
          :wk-tasks ['clean 'clean-hard]}]]]
      (is (= {:doc "for test"
              :wk-tasks ['clean 'clean-hard]
              :pf :bb
              :specific-cli-opts-kw [nil nil]}
             (->> (sut/update-registry-workflow-entries registry-stub)
                  (into {})
                  ('wk-test))))))
  (testing "One clj task is changing wofklow task status"
    (let
      [registry-stub
       [['clean
         {:doc "Clean cache files for compiles, and logs"
          :la-test {:cmd ["bb" "clean"]}
          :task-fn 'automaton-build-app.tasks.clean/clean}]
        ['clean-hard
         {:doc
          "Clean all files which are not under version control (it doesn't remove untracked file or staged files if there are eligible to `git add .`)"
          :la-test {:cmd ["bb" "clean-hard"]
                    :process-opts {:in "q"}}
          :pf :clj
          :task-fn 'automaton-build-app.tasks.clean-hard/clean-hard}]
        ['wk-test
         {:doc "for test"
          :wk-tasks ['clean 'clean-hard]}]]]
      (is (= :clj
             (->> (sut/update-registry-workflow-entries registry-stub)
                  (into {})
                  ('wk-test)
                  :pf))))))
