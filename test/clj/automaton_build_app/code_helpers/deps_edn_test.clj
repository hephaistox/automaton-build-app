(ns automaton-build-app.code-helpers.deps-edn-test
  (:require
   [automaton-build-app.code-helpers.deps-edn :as sut]
   [automaton-build-app.os.files :as files]
   [clojure.test :refer [deftest is testing]]))

(def tmp-dir
  (files/create-temp-dir))

(deftest get-deps-filename-test
  (testing "Get deps is working"
    (is (string? (sut/get-deps-filename tmp-dir)))))

(deftest extract-paths-test
  (testing "Extract paths from a deps.edn file"
    (is (= ["a" "b" "c" "d" "e" "f" "g"]
           (sut/extract-paths {:paths ["a" "b" "c"]
                               :aliases {:repl {:extra-paths ["d" "e"]}
                                         :runner {:extra-paths ["f" "g"]}}}
                              #{})))
    (is (= ["a" "b" "c" "d" "e" "f" "g"]
           (sut/extract-paths {:paths ["a" "b" "c"]
                               :aliases {:repl {:extra-paths ["d" "e"]}
                                         :runner {:extra-paths ["f" "g"]}}}))))
  (testing "Exclusion of aliases is working"
    (is (= ["a" "b" "c" "d" "e"]
           (sut/extract-paths {:paths ["a" "b" "c"]
                               :aliases {:repl {:extra-paths ["d" "e"]}
                                         :runner {:extra-paths ["f" "g"]}}}
                              #{:runner})))
    (is (= ["a" "b" "c" "f" "g"]
           (sut/extract-paths {:paths ["a" "b" "c"]
                               :aliases {:repl {:extra-paths ["d" "e"]}
                                         :runner {:extra-paths ["f" "g"]}}}
                              #{:repl}))))
  (testing "Dedupe works"
    (is (= ["a" "b" "c" "f" "g"]
           (sut/extract-paths {:paths ["f" "g"]
                               :aliases {:repl {:extra-paths ["d" "e" "f"]}
                                         :runner {:extra-paths ["a" "b" "c" "f"]}}}
                              #{:repl})))))
