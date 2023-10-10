(ns automaton-build-app.cicd.cfg-mgt-test
  (:require
   [automaton-build-app.cicd.cfg-mgt :as sut]
   [automaton-build-app.cicd.server :as build-cicd-server]
   [automaton-build-app.os.commands :as build-cmds]
   [clojure.test :refer [deftest is testing]]))

(deftest git-installed?*-test
  (when-not (build-cicd-server/is-cicd?)
    (testing "Is able to detect non working git"
      (is (not (sut/git-installed?* "non-git-command"))))))

(comment
  (zero?
   (ffirst
    (build-cmds/execute ["git" "-v"
                         {:out :string
                          :dir "."}])))

  (try
    (sut/push-local-dir-to-repo "/Users/anthonycaumond/Dev/hephaistox/monorepo/clojure/automaton/automaton_core"
                                "git@github.com:hephaistox/automaton-core.git"
                                "main"
                                "caumond/feature/core-is-autonomous_2"
                                "Manual test")
    (catch Exception e
      (println e)))

  (try
    (sut/extract-app-from-repo "git@github.com:hephaistox/monorepo.git"
                               "git@github.com:hephaistox/automaton-core.git"
                               "caumond/feature/core-is-autonomous_2"
                               "clojure/automaton/automaton_core"
                               "Manual test")
    (catch Exception e
      (println e)))

  (build-cmds/execute ["git" "commit" "-m" "test"
                       {:dir "/var/folders/fz/zf0944w113b5pj984_ywtb1m0000gn/T/898b096d-0f8b-4b9f-95b8-7c92af78b11d15756115773096826491"}])
;
  )
