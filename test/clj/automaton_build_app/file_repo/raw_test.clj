(ns automaton-build-app.file-repo.raw-test
  (:require
   [automaton-build-app.file-repo.raw :as sut]
   [automaton-build-app.file-repo.raw.impl-text :as sut-raw-impl]
   [clojure.test :refer [deftest is testing]]))

(def raw-files-repo
  (sut/->RawFilesRepository sut-raw-impl/raw-files-repo-map))

(deftest exclude-files
  (testing "Exclusion of no file is ok"
    (let [res (sut/exclude-files raw-files-repo
                                 #{"none"})]
      (is (map? (sut/file-repo-map res)))
      (is (= (sut/file-repo-map raw-files-repo)
             (sut/file-repo-map res))))))

(deftest file-repo-map-test
  (testing "file repo map returns the map"
    (is (map?
         (sut/file-repo-map raw-files-repo)))))

(deftest filter-repo-test
  (testing "Refusing all files is ok"
    (is (empty? (-> (sut/filter-repo raw-files-repo
                                     (constantly false))
                    sut/file-repo-map))))
  (testing "Nil filter function means keep nothing"
    (is (= sut-raw-impl/raw-files-repo-map
           (sut/file-repo-map
            (sut/filter-repo raw-files-repo
                             nil))))))

(deftest filter-by-extension-test
  (testing "limit to clj files"
    (is (= (select-keys sut-raw-impl/raw-files-repo-map
                        ["foo.clj"])
           (-> (sut/filter-by-extension raw-files-repo
                                        #{"clj"})
               sut/file-repo-map)))))
