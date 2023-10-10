(ns automaton-build-app.tasks.test
  "Tests"
  (:require
   [automaton-build-app.apps.app :as build-app]
   [automaton-build-app.os.commands :as build-cmds]
   [automaton-build-app.tasks.common :as build-tasks-common]))

(defn ltest
  "Local tests
  All are made to be executed on github
  `rlwrap` is not on the container image, so clojure should be used instead of `clj`
  Params:
  * `aliases` collection of aliases to use in the tests"
  [& aliases]
  (let [app-data (build-app/build-app-data "")]
    (-> (build-cmds/execute ["clojure" (apply str "-M"
                                              aliases)])
        last
        build-tasks-common/exit-code)
    (when (:shadow-cljs app-data)
      (->> (build-cmds/execute ["npm" "install"]
                               ["npx" "shadow-cljs" "compile" "ltest"]
                               ["npx" "karma" "start" "--single-run"])
           (map build-tasks-common/exit-code)))))
