(ns automaton-build-app.tasks.launcher.bb-entrypoint-test
  (:require [automaton-build-app.tasks.launcher.bb-entrypoint :as sut]
            [automaton-build-app.log :as build-log]))

(comment
  (sut/-main ["blog" "-l" "trace" "-d"])
  (sut/-main ["apps" "-l" "trace" "-d"])
  (sut/-main ["clean" "-l" "trace" "-d"])
  (sut/-main ["clean-hard" "-l" "trace" "-d"])
  (sut/-main ["gha" "-l" "trace" "-d"])
  (sut/-main ["gha" "-l" "trace" "-d"])
  (build-log/set-min-level! :trace)
  ;
)
