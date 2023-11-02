(ns automaton-build-app.app-test
  (:require [automaton-build-app.app :as sut]
            [automaton-build-app.code-helpers.build-config :as build-build-config]
            [clojure.test :refer [deftest is testing]]))

(def app-stub
  "Application build config as it is loaded on disk"
  {:monorepo {:app-dir "app_stub"}
   :publication {:repo {:address "git@github.com:hephaistox/app-stub.git"
                        :branch "main"}
                 :as-lib 'hephaistox/automaton-app-stub
                 :major-version "1.0.%d"
                 :jar {:class-dir "target/classes"
                       :excluded-aliases #{}
                       :target-filename "target/%s-%s.jar"}}
   :customer-materials {:html-dir "tmp/html"
                        :dir "customer_materials"
                        :pdf-dir "tmp/pdf"}
   :doc {:dir "docs"
         :archi {:dir "docs/archi/"}
         :code-stats {:output-file "docs/code/stats.md"}
         :code-doc {:dir "docs/code"
                    :title "Build app automaton"
                    :description "Autonomous project to build an hephaistox app"}}
   :clean {:compile-logs-dirs [".cpcache" ".clj-kondo/.cache" "tmp"]}
   :app-name "automaton-app-stub"})

(def built-app-stub
  "Application after the loading"
  (assoc app-stub
         :deps-edn {:paths ["src/clj"]
                    :aliases {:foo {:extra-paths ["src/cljc"]}
                              :cljs-paths {:extra-paths ["src/cljc" "src/cljs"]}}}
         :shadow-cljs {}))

(deftest validate-test
  (testing "Non valid app " (is (not (sut/valid? {:weird-property true}))))
  (testing "Valid app real build-config"
    (is (-> (build-build-config/read-build-config "")
            sut/valid?))))

(deftest build-app-test (testing "Current project should generate its map" (is (< 0 (count (@sut/build-app-data_ ""))))))

(deftest is-cust-app-but-template?-test
  (testing "Template app and non cust app are refused"
    (is (not (sut/is-cust-app-but-template? {:template-app? true})))
    (is (not (sut/is-cust-app-but-template? {}))))
  (testing "Cust app are accepted" (is (sut/is-cust-app-but-template? {:cust-app? true}))))

(deftest get-clj-c-src-dirs-test
  (testing "Returns some existing current source dir of the current app dir"
    (is (= 1 (count (sut/clj-compiler-classpath built-app-stub true))))))

(deftest get-cljc-s-src-dirs-test
  (testing "automaton-buil-app has no cljc or cljs files" (is (= 0 (count (sut/cljs-compiler-classpaths built-app-stub))))))

(deftest get-clj-c-s-src-dirs-test
  (testing "Returns some existing current source dir of the current app dir" (is (= 1 (count (sut/classpath-dirs built-app-stub))))))
