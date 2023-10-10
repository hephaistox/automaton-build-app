(ns automaton-build-app.code-helpers.report-test
  (:require
   [automaton-build-app.code-helpers.report :as sut]
   [clojure.test :refer [deftest is testing]]))

(def files-repo {"foo.clj"  ["This is"
                             " the foo file"
                             " hey!"]
                 "foo.edn" ["test"]
                 "bar.cljc" ["This is the bar file"
                             " ho ho!"]})

(deftest create-report-test
  (testing "Check report is well formed"
    (is (= [["foo.clj" "is"]
            ["bar.cljc" "is"]]
           (sut/create-report files-repo
                              #"is"))))
  (testing "Check grouped regexp"
    (is (= [["foo.clj" ["is" "is"]]
            ["bar.cljc" ["is" "is"]]]
           (sut/create-report files-repo
                              #"(is)"))))
  (testing "Gather the whole line"
    (is (= [["foo.clj" ["This is" "is"]]
            ["bar.cljc" ["This is the bar file" "is"]]]
           (sut/create-report files-repo
                              #".*(is).*"))))
  (testing "Check report allow empty results"
    (is (empty?
         (sut/create-report files-repo
                            #"ThisIsNotInTheRepo")))))

(deftest map-report-test
  (let [report (sut/create-report files-repo
                                  #"(is)")]
    (is (= [["foo.clj" "is" "is"]
            ["bar.cljc" "is" "is"]]
           (sut/map-report report
                           (fn [filename [whole-match match1]]
                             [filename whole-match match1]))))))

(deftest print-report-test
  (testing "Test the report is ok"
    (is (= "The liner is [\"foo.clj\" [\"is\" \"is\"]] !!\nThe liner is [\"bar.cljc\" [\"is\" \"is\"]] !!\n"
           (with-out-str
             (sut/print-report (sut/create-report files-repo
                                                  #"(is)")
                               (fn [line]
                                 (println (format "The liner is %s !!" line)))))))))
