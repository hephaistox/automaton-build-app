(ns automaton-build-app.file-repo.text-analyzis.regexp-test
  (:require
   [automaton-build-app.file-repo.text-analyzis.regexp :as sut]
   [automaton-build-app.file-repo.text :as build-file-repo]
   [clojure.test :refer [deftest is testing]]))

(def files-repo-map
  {"foo.clj"  ["This is"
               " the foo file"
               " hey!"]
   "foo.edn" ["test"]
   "bar.cljc" ["This is the bar file"
               " ho ho!"]})

(def files-repo
  (build-file-repo/->TextFilesRepository files-repo-map))

(def regexp-textfile
  (sut/make-regexp-Textfile files-repo
                            #"is"))

(deftest save-as-report-test
  (testing "Check report is well formed"
    (is (= [["foo.clj" "is"]
            ["bar.cljc" "is"]]
           (sut/make-regexp-Textfile files-repo
                                     #"is"))))
  #_(testing "Check grouped regexp"
      (is (= [["foo.clj" ["is" "is"]]
              ["bar.cljc" ["is" "is"]]]
             (sut/save-as-report files-repo
                                 #"(is)"))))
  #_(testing "Gather the whole line"
      (is (= [["foo.clj" ["This is" "is"]]
              ["bar.cljc" ["This is the bar file" "is"]]]
             (sut/save-as-report files-repo
                                 #".*(is).*"))))
  #_(testing "Check report allow empty results"
      (is (empty?
           (sut/save-as-report files-repo
                               #"ThisIsNotInTheRepo")))))

(comment
 ;;  (deftest create-report-test
;;   (testing "Check report is well formed"
;;     (is (= [["foo.clj" "is"]
;;             ["bar.cljc" "is"]]
;;            (sut/create-report files-repo
;;                               #"is"))))
;;   (testing "Check grouped regexp"
;;     (is (= [["foo.clj" ["is" "is"]]
;;             ["bar.cljc" ["is" "is"]]]
;;            (sut/create-report files-repo
;;                               #"(is)"))))
;;   (testing "Gather the whole line"
;;     (is (= [["foo.clj" ["This is" "is"]]
;;             ["bar.cljc" ["This is the bar file" "is"]]]
;;            (sut/create-report files-repo
;;                               #".*(is).*"))))
;;   (testing "Check report allow empty results"
;;     (is (empty?
;;          (sut/create-report files-repo
;;                             #"ThisIsNotInTheRepo")))))

;; (deftest map-report-test
;;   (let [report (sut/create-report files-repo
;;                                   #"(is)")]
;;     (is (= [["foo.clj" "is" "is"]
;;             ["bar.cljc" "is" "is"]]
;;            (sut/map-report report
;;                            (fn [filename [whole-match match1]]
;;                              [filename whole-match match1]))))))

;; (deftest print-report-test
;;   (testing "Test the report is ok"
;;     (is (= "The liner is [\"foo.clj\" [\"is\" \"is\"]] !!\nThe liner is [\"bar.cljc\" [\"is\" \"is\"]] !!\n"
;;            (with-out-str
;;              (sut/print-report (sut/create-report files-repo
;;                                                   #"(is)")
;;                                (fn [line]
;;                                  (println (format "The liner is %s !!" line)))))))))

;; ;
  )
