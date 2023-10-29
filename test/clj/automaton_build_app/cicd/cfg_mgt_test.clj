(ns automaton-build-app.cicd.cfg-mgt-test
  (:require [automaton-build-app.cicd.cfg-mgt :as sut]
            [automaton-build-app.cicd.server :as build-cicd-server]
            [clojure.test :refer [deftest is testing]]
            [automaton-build-app.os.files :as build-files]))

(deftest git-installed?*-test
  (when-not (build-cicd-server/is-cicd?)
    (testing "Is able to detect non working git"
      (is (not (sut/git-installed?* "non-git-command"))))))

(comment
  (sut/clean-hard "")
  (let [tmp-dir (build-files/create-temp-dir "test")]
    (build-files/create-dirs tmp-dir)
    (sut/clone-repo-branch tmp-dir
                           "git@github.com:hephaistox/automaton-core.git"
                           "main")
    (sut/create-and-switch-to-branch tmp-dir "test-branch")
    (println "branch is correct?:"
             (= "test-branch" (sut/current-branch tmp-dir))))
  (try
    (sut/push-local-dir-to-repo
      "/Users/anthonycaumond/Dev/hephaistox/monorepo/clojure/automaton/automaton_core"
      "git@github.com:hephaistox/automaton-core.git" "main"
      "caumond/feature/core-is-autonomous_2" "Manual test"
      "Manual test" "v-test-1.%s")
    (catch Exception e (println e)))
  (try (sut/extract-app-from-repo "git@github.com:hephaistox/monorepo.git"
                                  "git@github.com:hephaistox/automaton-core.git"
                                    "caumond/feature/core-is-autonomous_2"
                                  "clojure/automaton/automaton_core"
                                    "Manual test"
                                  "Manual test" "0.0.0")
       (catch Exception e (println e)))
  ;
)
