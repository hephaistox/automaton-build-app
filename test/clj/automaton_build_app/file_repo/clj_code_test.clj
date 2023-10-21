(ns automaton-build-app.file-repo.clj-code-test
  (:require
   [automaton-build-app.file-repo.clj-code :as sut]
   [automaton-build-app.file-repo.raw :as build-file-repo-raw]
   [clojure.test :refer [deftest is testing]]))

(def clj-repo-stub
  (sut/make-clj-repo-from-dirs [""]))

(deftest filter-by-usage-test
  (testing "Filter by usage is ok"
    (is (< 0
           (-> (sut/filter-by-usage clj-repo-stub
                                    :edn)
               count)))))

(deftest search-filenames-test
  (testing "Some clojure files are found in the current project"
    (is (< 0
           (-> (sut/search-clj-filenames ".")
               count)))))

(comment
  (prn (-> (sut/filter-by-usage (sut/make-clj-repo-from-dirs [""])
                                :reader)
           build-file-repo-raw/file-repo-map
           keys))
;
  )

(deftest make-clj-repo-test
  (testing "test repos for edn"
    (is (< 0
           (->
            (sut/make-clj-repo-from-dirs [""]
                               :edn)
            build-file-repo-raw/file-repo-map
            keys
            count))))
  (testing "test repos for clj"
    (is (< 0
           (->
            (sut/make-clj-repo-from-dirs [""]
                               :clj)
            build-file-repo-raw/file-repo-map
            keys
            count))))
  (testing "test to optional value :reader"
    (is (< 30
           (count
            (build-file-repo-raw/file-repo-map
             (sut/make-clj-repo-from-dirs [""])))))))

(comment
  (-> (sut/make-clj-repo-from-dirs ["src" "test"] :clj)
      :_file-repo-map
      count)
  ;
  )
