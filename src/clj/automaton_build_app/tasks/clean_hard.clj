(ns automaton-build-app.tasks.clean-hard
  (:require [automaton-build-app.cicd.cfg-mgt :as build-cfg-mgt]
            [automaton-build-app.os.files :as build-files]
            [automaton-build-app.os.exit-codes :as build-exit-codes]))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn exec
  "Clean the repository to the state as it's after being cloned from git server"
  [_task-map {:keys [app-dir]}]
  (let [clean-res (-> (build-files/absolutize app-dir)
                      build-cfg-mgt/clean-hard)]
    (cond (= ::build-cfg-mgt/git-not-installed clean-res) build-exit-codes/cannot-execute
          (true? clean-res) build-exit-codes/ok
          :else build-exit-codes/catch-all)))
