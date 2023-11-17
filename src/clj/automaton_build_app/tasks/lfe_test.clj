(ns automaton-build-app.tasks.lfe-test
  (:require [automaton-build-app.code-helpers.frontend-compiler :as build-frontend-compiler]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.os.commands :as build-cmds]
            [automaton-build-app.os.exit-codes :as build-exit-codes]))

(defn lfe-test
  "Local tests
  All that tests should be runnable on github action
  `rlwrap` is not on the container image, so `clojure` should be used instead of `clj`"
  [_cli-opts
   {:keys [app-dir]
    :as app} _bb-edn-args]
  (let [{:keys [shadow-cljs]} app]
    (when-not (or (not shadow-cljs)
                  (apply build-cmds/execute-and-trace
                         (concat [["npm" "install"]]
                                 (mapv (fn [build] ["npx" "shadow-cljs" "compile" (str build)]) (build-frontend-compiler/builds app-dir))
                                 [["npx" "karma" "start" "--single-run"]])))
      (build-log/fatal "Tests have failed")
      (System/exit build-exit-codes/catch-all))))
