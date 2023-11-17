(ns automaton-build-app.app-test
  (:require [automaton-build-app.app :as sut]
            [automaton-build-app.app.build-config :as build-build-config]
            [automaton-build-app.schema :as build-schema]
            [clojure.test :refer [deftest is testing]]))

(def built-app-stub
  "Application after the loading"
  {:build-config {}
   :deps-edn {:paths ["src/clj"]
              :aliases {:foo {:extra-paths ["src/cljc"]}
                        :cljs-paths {:extra-paths ["src/cljc" "src/cljs"]}}}
   :shadow-cljs {}})

(deftest validate-test
  (testing "Non valid app " (is (not (build-schema/valid? build-build-config/build-config-schema {:weird-property true}))))
  (let [local-build-config (build-build-config/read-build-config "")]
    (testing "Valid app real build-config" (is (build-schema/valid? build-build-config/build-config-schema local-build-config)))
    (testing "Invalid schema are found" (is (nil? (build-schema/valid? [:vector :string] local-build-config))))))

(deftest build-app-test (testing "Current project should generate its map" (is (< 0 (count (sut/build ""))))))

(deftest classpath-dirs-test
  (testing "Returns some existing current source dir of the current app dir" (is (= 3 (count (sut/classpath-dirs built-app-stub))))))
