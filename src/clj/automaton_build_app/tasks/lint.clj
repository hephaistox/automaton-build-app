(ns automaton-build-app.tasks.lint
  (:require [automaton-build-app.app :as build-app]
            [automaton-build-app.code-helpers.lint :as build-lint]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.os.exit-codes :as build-exit-codes]))

(defn lint
  "Linter"
  [{:keys [min-level], :as _parsed-cli-opts}]
  (build-log/set-min-level! min-level)
  (let [app-dir ""
        app-data (@build-app/build-app-data_ app-dir)]
    (when-not (build-lint/lint false (build-app/src-dirs app-data))
      (build-log/fatal "Tests have failed")
      (System/exit build-exit-codes/catch-all))))
