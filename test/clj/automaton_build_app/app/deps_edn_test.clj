(ns automaton-build-app.app.deps-edn-test
  (:require [automaton-build-app.app.deps-edn :as sut]
            [automaton-build-app.os.files :as build-files]
            [clojure.test :refer [deftest is testing]]))

(def tmp-dir (build-files/create-temp-dir))

(deftest get-deps-filename-test (testing "Get deps is working" (is (string? (sut/get-deps-filename tmp-dir)))))

(deftest extract-paths-test
  (testing "Extract paths from a deps.edn file"
    (is (= ["a" "b" "c" "d" "e" "f" "g"]
           (sut/extract-paths {:paths ["a" "b" "c"]
                               :aliases {:repl {:extra-paths ["d" "e"]}
                                         :runner {:extra-paths ["f" "g"]}}}
                              #{})))
    (is (= ["a" "b" "c" "d" "e" "f" "g"]
           (sut/extract-paths {:paths ["a" "b" "c"]
                               :aliases {:repl {:extra-paths ["d" "e"]}
                                         :runner {:extra-paths ["f" "g"]}}}
                              #{}))))
  (testing "Exclusion of aliases is working"
    (is (= ["a" "b" "c" "d" "e"]
           (sut/extract-paths {:paths ["a" "b" "c"]
                               :aliases {:repl {:extra-paths ["d" "e"]}
                                         :runner {:extra-paths ["f" "g"]}}}
                              #{:runner})))
    (is (= ["a" "b" "c" "f" "g"]
           (sut/extract-paths {:paths ["a" "b" "c"]
                               :aliases {:repl {:extra-paths ["d" "e"]}
                                         :runner {:extra-paths ["f" "g"]}}}
                              #{:repl}))))
  (testing "Dedupe works"
    (is (= ["a" "b" "c" "f" "g"]
           (sut/extract-paths {:paths ["f" "g"]
                               :aliases {:repl {:extra-paths ["d" "e" "f"]}
                                         :runner {:extra-paths ["a" "b" "c" "f"]}}}
                              #{:repl})))))

(deftest load-deps-edn-test
  (testing "Existing file is found" (is (map? (sut/load-deps-edn ""))))
  (testing "Non existing file is ok" (is (nil? (sut/load-deps-edn "non-existing-dir")))))

(deftest is-hephaistox-deps-test
  (testing "Hephaistox lib find" (is (sut/is-hephaistox-deps ['hephaistox/automaton-build-app {:mvn/version ""}])))
  (testing "Non hephaistox lib is skipped" (is (not (sut/is-hephaistox-deps [:is-a-dep {:mvn/version ""}])))))

(deftest hephaistox-deps-test
  (testing "Hephaistox deps are selected"
    (is (= ['hephaistox/automaton-build-app]
           (sut/hephaistox-deps {:deps {'hephaistox/automaton-build-app {}
                                        'clojure.core {}}})))))

(deftest update-dep-local-root-test
  (testing "Not concerned deps are unchanged"
    (is (= {} (sut/update-dep-local-root "" {})))
    (is (= nil (sut/update-dep-local-root "" nil)))
    (is (= {:foo :bar} (sut/update-dep-local-root "" {:foo :bar}))))
  (testing "local roots are updated"
    (is (= {:local/root "../../automaton/"} (sut/update-dep-local-root ".." {:local/root "../automaton"})))
    (is (= {:local/root "../../automaton/"
            :foo :bar}
           (sut/update-dep-local-root ".."
                                      {:local/root "../automaton"
                                       :foo :bar})))))

(deftest update-alias-local-root-test
  (testing "No modification if the alias contains no local/root"
    (is (= {:extra-deps {}
            :deps {}}
           (sut/update-alias-local-root ".."
                                        {:extra-deps {}
                                         :deps {}})))
    (is (= {:deps {}} (sut/update-alias-local-root ".." {:deps {}})))
    (is (= {:extra-deps {}} (sut/update-alias-local-root ".." {:extra-deps {}}))))
  (testing "deps and extra-deps local-root's are updated "
    (is (= {:deps {'my-lib1 {:local/root "../../my-lib1/"
                             :foo :bar}}
            :extra-deps {'my-lib2 {:local/root "../../my-lib2/"
                                   :bar :foo}}}
           (sut/update-alias-local-root ".."
                                        {:deps {'my-lib1 {:local/root "../my-lib1"
                                                          :foo :bar}}
                                         :extra-deps {'my-lib2 {:local/root "../my-lib2"
                                                                :bar :foo}}})))))

(deftest update-aliases-local-root-test
  (testing "All aliases are updated"
    (is (= {:alias-1 {:extra-deps {'lib1 {:local/root "../../lib1/"}}
                      :deps {'lib2 {:local/root "../../lib2/"}}}
            :alias-2 {:extra-deps {'lib2 {:local/root "../../lib2/"}}
                      :deps {'lib3 {:local/root "../../../lib3/"}}}}
           (sut/update-aliases-local-root ".."
                                          {:alias-1 {:extra-deps {'lib1 {:local/root "../lib1"}}
                                                     :deps {'lib2 {:local/root "../lib2"}}}
                                           :alias-2 {:extra-deps {'lib2 {:local/root "../lib2"}}
                                                     :deps {'lib3 {:local/root "../../lib3"}}}})))))
