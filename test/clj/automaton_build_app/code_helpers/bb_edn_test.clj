(ns automaton-build-app.code-helpers.bb-edn-test
  (:require [automaton-build-app.code-helpers.bb-edn :as sut]
            [automaton-build-app.os.files :as build-files]
            [clojure.test :refer [deftest is testing]]))

(def tmp-bb-dir (build-files/create-temp-dir))
(def update-fn
  (fn [bb-edn]
    (assoc bb-edn
           :new-tasks
           {"foo4" {:cli-params-mode :none
                    :doc "Test"
                    :exec-task 'bar}
            "foo2" {:cli-params-mode :none
                    :doc "Test2"
                    :exec-task 'bar2}})))
(deftest update-bb-edn-test
  (testing "Update edn keep `:init` and `:requires` keys, and add what is in tasks"
    (let [tmp-bb-dir (build-files/create-temp-dir)
          update-fn (fn [bb-edn]
                      (assoc bb-edn
                             :new-tasks
                             {"foo4" {:cli-params-mode :none
                                      :doc "Test"
                                      :exec-task 'bar}
                              "foo2" {:cli-params-mode :none
                                      :doc "Test2"
                                      :exec-task 'bar2}}))]
      (build-files/spit-file (build-files/create-file-path tmp-bb-dir sut/bb-edn-filename)
                             {:paths []
                              :deps {}
                              :tasks {:init "don't touch"
                                      :requires "neither"}})
      (is (not (sut/update-bb-edn tmp-bb-dir update-fn)))
      (is (sut/update-bb-edn tmp-bb-dir update-fn)))))
