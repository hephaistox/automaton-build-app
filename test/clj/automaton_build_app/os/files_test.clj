(ns automaton-build-app.os.files-test
  (:require
   [automaton-build-app.os.files :as sut]
   [babashka.fs :as fs]
   [clojure.java.io :as io]
   [clojure.test :refer [deftest is testing]]))

(deftest copy-files-or-dir
  (let [tmp-dir (fs/create-temp-dir)]
    (testing "Directory copy"
      (do
        (sut/copy-files-or-dir [(io/resource "os/resource-test-copy-dir")]
                               tmp-dir)
        (is (= #{"test1" "test2"}
               (into #{}
                     (map (fn [file]
                            (str (fs/relativize tmp-dir file)))

                          (fs/glob tmp-dir "**"))))))))
  (testing "If files are not a vector of string"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"parameter should be a sequence"
                          (sut/copy-files-or-dir {} "/tmp"))))
  (let [tmp-dir (fs/create-temp-dir)]
    (testing "File copy"
      (is (= (do
               (sut/copy-files-or-dir [(io/resource (str "os/resource-test-copy-dir"
                                                         fs/file-separator
                                                         "test1"))]
                                      tmp-dir)
               #{"test1"})
             (into #{}
                   (map (fn [file]
                          (str (fs/relativize tmp-dir file)))
                        (fs/glob tmp-dir "**"))))))))

(deftest directory-exists?
  (testing "A non existing directory is detected"
    (is (not
         (sut/directory-exists? "non-existing-directory"))))
  (testing "A non existing directory in existing directory is detected"
    (is (not
         (sut/directory-exists? "everything/non-existing"))))
  (testing "A file is detected as a non directory"
    (is (not
         (sut/directory-exists? "deps.edn"))))
  (testing "An existing directory is detected"
    (is (sut/directory-exists? ".clj-kondo")))
  (testing "An existing directory inside directory is detected"
    (is (sut/directory-exists? ".clj-kondo/rewrite-clj"))))

(deftest is-existing-file?
  (testing "A non existing path is accepted"
    (is (not (sut/is-existing-file? "non-existing-directory"))))
  (testing "An already existing path is accepted"
    (is (sut/is-existing-file? "deps.edn"))))

(deftest create-temp-dir
  (testing "A temporary directory has really been created"
    (is (sut/directory-exists? (sut/create-temp-dir)))))

(deftest remove-trailing-separator
  (let [base-dir (str sut/file-separator "tmp" sut/file-separator "foo")]
    (testing "Accept directories with no trailing separator"
      (is (= base-dir
             (sut/remove-trailing-separator base-dir))))
    (testing "Remove one trailing separator"
      (is (= base-dir
             (sut/remove-trailing-separator (str base-dir sut/file-separator)))))
    (testing "Remove one trailing separator"
      (is (= base-dir
             (sut/remove-trailing-separator (str base-dir sut/file-separator sut/file-separator)))))
    (testing "Remove one trailing separator"
      (is (= base-dir
             (sut/remove-trailing-separator (str base-dir sut/file-separator " ")))))
    (testing "Remove one trailing separator"
      (is (= base-dir
             (sut/remove-trailing-separator (str " " base-dir sut/file-separator)))))
    (testing "Remove one trailing separator"
      (is (= base-dir
             (sut/remove-trailing-separator (str " " base-dir sut/file-separator " ")))))))

(deftest create-dir-path
  (let [expected-result (str sut/file-separator "tmp" sut/file-separator "foo" sut/file-separator "bar" sut/file-separator)]
    (testing "Creates a simple path"
      (is (= expected-result
             (sut/create-dir-path sut/file-separator "tmp" "foo" "bar"))))
    (testing "Don't add path separator if already there"
      (is (= expected-result
             (sut/create-dir-path (str) sut/file-separator
                                  "tmp" "foo" "bar"))))
    (testing "Empty strings are filtered"
      (is (= expected-result
             (sut/create-dir-path (str) sut/file-separator "tmp" "" "foo" "bar"))))
    (testing "nil path returns nil"
      (is (= "./"
             (sut/create-dir-path))))
    (testing "Trailing separator is not added if already there"
      (is (= expected-result
             (sut/create-dir-path sut/file-separator "tmp" "foo" "bar"))))
    (testing "Relative path are working also"
      (is (= (str "tmp" sut/file-separator "foo" sut/file-separator "bar" sut/file-separator)
             (sut/create-dir-path "tmp" "foo" "bar"))))))

(deftest create-file-path
  (let [expected-result (str sut/file-separator "tmp" sut/file-separator "foo" sut/file-separator "bar")]
    (testing "Creates a simple path"
      (is (= expected-result
             (sut/create-file-path sut/file-separator "tmp" "foo" "bar"))))
    (testing "Don't add path separator if already there"
      (is (= expected-result
             (sut/create-file-path sut/file-separator (str) "tmp" "foo" "bar"))))
    (testing "Empty strings are filtered"
      (is (= expected-result
             (sut/create-file-path sut/file-separator (str) "tmp" "" "foo" "bar"))))
    (testing "No parameters creates a root dir"
      (is (= "."
             (sut/create-file-path))))
    (testing "Trailing separator is not added if already there"
      (is (= expected-result
             (sut/create-file-path sut/file-separator "tmp" "foo" "bar"))))
    (testing "Relative path are working also"
      (is (= (str "tmp" sut/file-separator "foo" sut/file-separator "bar")
             (sut/create-file-path "tmp" "foo" "bar"))))))

(deftest absolutize
  (testing "Absolute don't change an absolute path"
    (is (= "/foo"
           (sut/absolutize "/foo"))))
  (testing "Absolute changes a relative path"
    (is (not= "foo/bar"
              (sut/absolutize "foo/bar"))))
  (testing "Nil is not failing"
    (is (nil?
         (sut/absolutize nil)))))

(deftest file-name
  (testing "Extract file name"
    (is (= "baz"
           (sut/file-name "/foo/bar/baz")))))

(deftest file-in-same-dir
  (testing "Empty root directory is ok"
    (is (= "foo"
           (sut/file-in-same-dir "" "foo"))))
  (testing "If the source is directory, the files are stored in it"
    (is (= ".clj-kondo/foo"
           (sut/file-in-same-dir ".clj-kondo" "foo"))))
  (testing "If the source is file, stored in the same parent"
    (is (= "automaton/automaton-core/foo"
           (sut/file-in-same-dir "automaton/automaton-core/deps.edn" "foo"))))
  (testing "If the source is file does not exist, store in the same parent"
    (is (= "automaton/automaton-core/foo"
           (sut/file-in-same-dir "automaton/automaton-core/deps-does-not-exist.edn" "foo")))))

(deftest extract-path-test
  (testing "A relative file returns nil"
    (is (= "./"
           (sut/extract-path "README.md"))))
  (testing "An absolute file returns nil"
    (is (= "/usr/bin/"
           (sut/extract-path "/usr/bin/ls"))))
  (testing "Extract a path from a file"
    (is (= "/foo/"
           (sut/extract-path "/foo/bar")))
    (is (= "/foo/bar/foo2/"
           (sut/extract-path "/foo/bar/foo2/bar2")))))

(deftest filter-existing-dir-test
  (testing "Only existing dir are filtered"
    (is (= 1
           (count
            (sut/filter-existing-dir [".clj-kondo" "non-existing-dir-filtered"]))))))

(deftest match-extension?-test
  (testing "Check extenstion is matching"
    (is (sut/match-extension? "foo.bar"
                              ".bar"))
    (is (sut/match-extension? "foo.bar"
                              ".txt" ".bar")))
  (testing "Check other extenstion are not matching"
    (is (not (sut/match-extension? "foo.bar")))
    (is (not (sut/match-extension? "foo.bar.mp4"
                                   ".txt" ".bar")))))
