(ns automaton-build-app.apps.app-test
  (:require
   [automaton-build-app.apps.app :as sut]
   [clojure.test :refer [deftest is testing]]))

(def app-stub
  "Application build config as it is loaded on disk"
  {:monorepo {:app-dir "app_stub"}
   :publication   {:repo-address "git@github.com:hephaistox/app-stub.git"
                   :repo-name "app-stub"
                   :as-lib 'app-stub
                   :branch "main"}
   :templating {:app-title "app stub "}

   :app-name "app-stub"
   :cust-app? true})

(def built-app-stub
  "Application after the loading"
  (assoc app-stub
         :deps-edn {:paths ["src/clj"]
                    :aliases {:foo {:extra-paths ["src/cljc"]}
                              :cljs-paths {:extra-paths ["src/cljc" "src/cljs"]}}}
         :shadow-cljs {}))

(deftest validate-test
  (testing "Valid app"
    (is (sut/valid? app-stub)))
  (testing "Non valid app "
    (is (not (sut/valid? {:weird-property true})))))

(deftest build-app-test
  (testing "Current project should generate its map"
    (is (< 0
           (count (sut/build-app-data ""))))))

(deftest is-cust-app-but-template?-test
  (testing "Template app and non cust app are refused"
    (is (not (sut/is-cust-app-but-template? {:template-app? true})))
    (is (not (sut/is-cust-app-but-template? {}))))
  (testing "Cust app are accepted"
    (is (sut/is-cust-app-but-template? {:cust-app? true}))))

(deftest get-clj-c-src-dirs-test
  (testing "Returns some existing current source dir of the current app dir"
    (is (= 1
           (count (sut/get-clj-c-src-dirs built-app-stub))))))

(deftest get-cljc-s-src-dirs-test
  (testing "automaton-buil-app has no cljc or cljs files"
    (is (= 0
          (count (sut/get-cljc-s-src-dirs built-app-stub))))))

(deftest get-clj-c-s-src-dirs-test
  (testing "Returns some existing current source dir of the current app dir"
    (is (= 1
           (count (sut/get-clj-c-s-src-dirs built-app-stub))))))

(deftest clj-c-s-files-repo-test
  (testing "Context of the test assertions"
    (is (map?
         (sut/clj-c-s-files-repo built-app-stub)))))
