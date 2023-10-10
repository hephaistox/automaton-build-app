(ns automaton-build-app.cicd.cljs-compiler-test
  (:require
   [automaton-build-app.cicd.cljs-compiler :as sut]
   [automaton-build-app.os.files :as files]
   [clojure.java.io :as io]
   [clojure.test :refer [deftest is testing]]))

(def shadow-cljs-dir
  (files/extract-path (str (io/file (io/resource "cicd/shadow-cljs.edn")))))

(deftest load-shadow-cljs-test
  (testing "Test a map is loaded"
    (is (map? (sut/load-shadow-cljs shadow-cljs-dir)))))

(deftest extract-paths-test
  (testing "Paths are extracted"
    (let [dirs (sut/extract-paths (sut/load-shadow-cljs shadow-cljs-dir))]
      (is (every? string? dirs))
      (is (> (count dirs) 3)))))
