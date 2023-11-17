(ns automaton-build-app.tasks.launcher.bb-entrypoint-test
  (:require [automaton-build-app.tasks.launcher.bb-entrypoint :as sut]
            [automaton-build-app.log :as build-log]))

(comment
  (sut/execute-task* 'blog nil nil ["-l" "trace" "-d"])
  (sut/execute-task* 'apps nil nil ["-l" "trace" "-d"])
  (sut/execute-task* 'clean nil nil ["-l" "trace" "-d"])
  (sut/execute-task* 'clean-hard nil nil ["-l" "trace" "-d"])
  (sut/execute-task* 'gha nil nil ["-l" "trace" "-d"])
  (sut/execute-task* 'gha nil nil ["-l" "trace" "-d"])
  (build-log/set-min-level! :trace)
  ;
)
