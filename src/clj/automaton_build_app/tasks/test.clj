(ns automaton-build-app.tasks.test
  "Tests"
  (:require
   [automaton-build-app.os.commands :as build-cmds]
   [automaton-build-app.tasks.common :as build-tasks-common]))

(defn ltest
  "Local tests
  All are made to be executed on github
  `rlwrap` is not on the container image, so clojure should be used instead of `clj`
  Params:
  * `aliases` collection of aliases to use in the tests"
  [& aliases]
  (-> (build-cmds/execute ["clojure" (apply str "-M"
                                         aliases)])
      last
      build-tasks-common/exit-code))

;;TODO
;;#_["npm" "install"]
;; #_["npx" "shadow-cljs" "compile" "ltest"]
;; #_["npx" "karma" "start" "--single-run"]
