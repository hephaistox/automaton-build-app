(ns automaton-build-app.doc.markdown-test
  (:require
   [automaton-build-app.doc.markdown :as sut]
   [automaton-build-app.log :as build-log]
   [automaton-build-app.os.files :as build-files]
   [clojure.test :refer [deftest is testing]]))

(deftest create-md-test
  (testing "Try to create a markdown file"
    (let [filename (build-files/create-file-path (build-files/create-temp-dir)
                                           "test")
          file-content "test"]
      (build-log/trace "`md` file created: " filename)
      (sut/create-md filename
                     file-content)
      (is (= file-content
             (slurp filename))))))
