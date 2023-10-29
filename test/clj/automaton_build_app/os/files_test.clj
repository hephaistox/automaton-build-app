(ns automaton-build-app.os.files-test
  (:require [automaton-build-app.os.files :as sut]
            [babashka.fs :as fs]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]))

;; ***********************
;; Manipulate file path (need no access)
;; ***********************

(deftest change-extension-test
  (testing "Empty files are ok"
    (is (= ".mov" (sut/change-extension "" ".mov"))))
  (testing "No extensions means removing the extension"
    (is (= "" (sut/change-extension "" nil) (sut/change-extension "" ""))))
  (testing "Filename is changed"
    (is (= "foo.mov" (sut/change-extension "foo" ".mov"))))
  (testing "Relative path is ok"
    (is (= "foo/bar.mov" (sut/change-extension "foo/bar.txt" ".mov"))))
  (testing "Absolute path is ok"
    (is (= "/tmp/foo.mov" (sut/change-extension "/tmp/foo.bar" ".mov")))))

(deftest remove-trailing-separator-test
  (let [base-dir (str sut/file-separator "tmp" sut/file-separator "foo")]
    (testing "Accept directories with no trailing separator"
      (is (= base-dir (sut/remove-trailing-separator base-dir))))
    (testing "Remove one trailing separator"
      (is (= base-dir
             (sut/remove-trailing-separator (str base-dir
                                                 sut/file-separator)))))
    (testing "Remove one trailing separator"
      (is (= base-dir
             (sut/remove-trailing-separator
               (str base-dir sut/file-separator sut/file-separator)))))
    (testing "Remove one trailing separator"
      (is (= base-dir
             (sut/remove-trailing-separator
               (str base-dir sut/file-separator " ")))))
    (testing "Remove one trailing separator"
      (is (= base-dir
             (sut/remove-trailing-separator
               (str " " base-dir sut/file-separator)))))
    (testing "Remove one trailing separator"
      (is (= base-dir
             (sut/remove-trailing-separator
               (str " " base-dir sut/file-separator " ")))))))

(deftest remove-useless-path-elements-test
  (testing "Remove double separators"
    (is (= "/User/john-doe/videos"
           (sut/remove-useless-path-elements "/User/john-doe//videos")))
    (is (= "/User/john-doe/videos/"
           (sut/remove-useless-path-elements "/User/john-doe///videos/")
           (sut/remove-useless-path-elements
             "/////User///john-doe/////videos////"))))
  (testing "Remove current dir as a subdir"
    (is (= "User/john-doe/videos"
           (sut/remove-useless-path-elements "User/./john-doe/./videos"))))
  (testing "No path"
    (is (= "" (sut/remove-useless-path-elements "")))
    (is (nil? (sut/remove-useless-path-elements nil)))))

(deftest create-file-path-test
  (let [expected-result (str sut/file-separator
                             "tmp"
                             sut/file-separator
                             "foo"
                             sut/file-separator
                             "bar")]
    (testing "Creates a simple path"
      (is (= expected-result
             (sut/create-file-path sut/file-separator "tmp" "foo" "bar"))))
    (testing "No dir provided returns no path"
      (is (nil? (sut/create-file-path)))
      (is (= "" (sut/create-file-path ""))))
    (testing "Don't add path separator if already there"
      (is (=
            expected-result
            (sut/create-file-path sut/file-separator (str) "tmp" "foo" "bar"))))
    (testing "Empty strings are filtered"
      (is (= expected-result
             (sut/create-file-path sut/file-separator
                                   (str)
                                   "tmp" ""
                                   "foo" "bar"))))
    (testing "Trailing separator is not added if already there"
      (is (= expected-result
             (sut/create-file-path sut/file-separator "tmp" "foo" "bar"))))
    (testing "Relative path are working also"
      (is (= (str "tmp" sut/file-separator "foo" sut/file-separator "bar")
             (sut/relativize-to-pwd
               (sut/create-file-path "tmp" "foo" "bar")))))))

(deftest create-dir-path-test
  (testing "Relative path is built and finish with separator"
    (is (= "tmp/foo/bar/" (sut/create-dir-path "tmp" "foo" "bar"))))
  (testing "Empty string is a relative path" (is (= "" (sut/create-dir-path)))))

(deftest create-absolute-dir-path-test
  (testing "Relative path is built and finish with separator"
    (is (= "/tmp/foo/bar/" (sut/create-absolute-dir-path "tmp" "foo" "bar")))))

(deftest match-extension?-test
  (testing "Check extenstion is matching"
    (is (sut/match-extension? "foo.bar" ".bar"))
    (is (sut/match-extension? "foo.bar" ".txt" ".bar")))
  (testing "Check other extenstion are not matching"
    (is (not (sut/match-extension? "foo.bar")))
    (is (not (sut/match-extension? "foo.bar.mp4" ".txt" ".bar")))))

(deftest file-in-same-dir-test
  (testing "Empty root directory is ok"
    (is (= "foo" (sut/relativize-to-pwd (sut/file-in-same-dir "" "foo")))))
  (testing "If the source is directory, the files are stored in it"
    (is (= ".clj-kondo/foo"
           (sut/relativize-to-pwd (sut/file-in-same-dir ".clj-kondo" "foo")))))
  (testing "If the source is file, stored in the same parent"
    (is (= "automaton/automaton-core/foo"
           (sut/relativize-to-pwd (sut/file-in-same-dir
                                    "automaton/automaton-core/deps.edn"
                                    "foo")))))
  (testing "If the source is file does not exist, store in the same parent"
    (is (= "automaton/automaton-core/foo"
           (sut/relativize-to-pwd
             (sut/file-in-same-dir
               "automaton/automaton-core/deps-does-not-exist.edn"
               "foo"))))))

(deftest is-absolute?-test
  (testing "Relative path" (is (not (sut/is-absolute? "foo/bar"))))
  (testing "Empty relative path" (is (not (sut/is-absolute? ""))))
  (testing "Absolute path" (is (sut/is-absolute? "/foo")))
  (testing "Empty absolute path" (is (sut/is-absolute? "/"))))

(deftest extract-path-test
  (testing "A relative file returns nil"
    (is (= "." (sut/relativize-to-pwd (sut/extract-path "README.md")))))
  (testing "An absolute file returns nil"
    (is (= "/usr/bin/" (sut/extract-path "/usr/bin/ls"))))
  (testing "Extract a path from a file"
    (is (= "/foo/" (sut/extract-path "/foo/bar")))
    (is (= "/foo/bar/foo2/" (sut/extract-path "/foo/bar/foo2/bar2")))))

;; ***********************
;; Change files on disk
;; ***********************

(deftest absolutize-test
  (testing "Absolute don't change an absolute path"
    (is (= "/foo" (sut/absolutize "/foo"))))
  (testing "Absolute changes a relative path"
    (is (not= "foo/bar" (sut/absolutize "foo/bar"))))
  (testing "Nil is not failing" (is (nil? (sut/absolutize nil)))))

(deftest copy-files-or-dir-test
  (let [tmp-dir (fs/create-temp-dir)]
    (testing "Directory copy"
      (do (sut/copy-files-or-dir [(io/resource "os/resource-test-copy-dir")]
                                 tmp-dir)
          (is (= #{"test1" "test2"}
                 (into #{}
                       (map (fn [file] (str (fs/relativize tmp-dir file)))
                         (fs/glob tmp-dir "**"))))))))
  (testing "If files are not a vector of string"
    (is (not (sut/copy-files-or-dir {} "/tmp"))))
  (let [tmp-dir (fs/create-temp-dir)]
    (testing "File copy"
      (is (= (do (sut/copy-files-or-dir [(io/resource
                                           (str "os/resource-test-copy-dir"
                                                fs/file-separator
                                                "test1"))]
                                        tmp-dir)
                 #{"test1"})
             (into #{}
                   (map (fn [file] (str (fs/relativize tmp-dir file)))
                     (fs/glob tmp-dir "**"))))))))

(deftest current-dir-test
  (testing "Current dir is an existing dir"
    (is (-> (sut/current-dir)
            sut/is-existing-dir?))))

(deftest directory-exists?-test
  (testing "A non existing directory is detected"
    (is (not (sut/directory-exists? "non-existing-directory"))))
  (testing "A non existing directory in existing directory is detected"
    (is (not (sut/directory-exists? "everything/non-existing"))))
  (testing "A file is detected as a non directory"
    (is (not (sut/directory-exists? "deps.edn"))))
  (testing "An existing directory is detected"
    (is (sut/directory-exists? ".clj-kondo")))
  (testing "An existing directory inside directory is detected"
    (is (sut/directory-exists? ".clj-kondo/rewrite-clj"))))

(deftest is-existing-file?-test
  (testing "A non existing path is accepted"
    (is (not (sut/is-existing-file? "non-existing-directory"))))
  (testing "An already existing path is accepted"
    (is (sut/is-existing-file? "deps.edn"))))

(deftest filter-to-existing-files-test
  (testing "Non existing files are removed"
    (is (= ["deps.edn" "bb.edn"]
           (sut/filter-to-existing-files "deps.edn"
                                         "non-existing-file"
                                         "bb.edn")))))

(deftest is-existing-dir?-test
  (testing "Current dir is an existing dir"
    (is (-> (sut/current-dir)
            sut/is-existing-dir?))))

(deftest create-temp-dir
  (testing "A temporary directory has really been created"
    (is (-> (sut/create-temp-dir)
            sut/directory-exists?))))

(deftest create-dir-path
  (let [expected-result (str sut/file-separator
                             "tmp" sut/file-separator
                             "foo" sut/file-separator
                             "bar" sut/file-separator)]
    (testing "Creates a simple path"
      (is (= expected-result
             (sut/create-dir-path sut/file-separator "tmp" "foo" "bar"))))
    (testing "Don't add path separator if already there"
      (is (= expected-result
             (sut/create-dir-path (str) sut/file-separator "tmp" "foo" "bar"))))
    (testing "Empty strings are filtered"
      (is
        (=
          expected-result
          (sut/create-dir-path (str) sut/file-separator "tmp" "" "foo" "bar"))))
    (testing "nil path returns current dir"
      (is (string? (sut/create-dir-path))))
    (testing "Trailing separator is not added if already there"
      (is (= expected-result
             (sut/create-dir-path sut/file-separator "tmp" "foo" "bar"))))
    (testing "Relative path are working also"
      (is (= (str "tmp" sut/file-separator
                  "foo" sut/file-separator
                  "bar" sut/file-separator)
             (sut/relativize-to-pwd
               (sut/create-dir-path "tmp" "foo" "bar")))))))

(deftest filename-test
  (testing "Extract file name" (is (= "baz" (sut/filename "/foo/bar/baz")))))

(deftest filter-existing-dir-test
  (testing "Only existing dir are filtered"
    (is (= 1
           (count (sut/filter-existing-dir [".clj-kondo"
                                            "non-existing-dir-filtered"]))))))
