(ns automaton-build-app.tasks.ltest
  (:require [automaton-build-app.app :as build-app]
            [automaton-build-app.code-helpers.frontend-compiler :as build-frontend-compiler]
            [automaton-build-app.code-helpers.lint :as build-lint]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.os.commands :as build-cmds]
            [automaton-build-app.os.exit-codes :as build-exit-codes]))

(defn ltest
  "Local tests
  All that tests should be runnable on github action
  `rlwrap` is not on the container image, so `clojure` should be used instead of `clj`"
  [{:keys [min-level]
    :as _parsed-cli-opts}]
  (build-log/set-min-level! min-level)
  (let [app-dir ""
        {:keys [ltest shadow-cljs]
         :as app-data}
        (@build-app/build-app-data_ app-dir)
        aliases (get ltest :aliases)]
    (when-not (and (build-lint/lint false (build-app/src-dirs app-data))
                   (build-cmds/execute-and-trace ["clojure" (apply str "-M" aliases)])
                   (or (not shadow-cljs)
                       (apply build-cmds/execute-and-trace
                              (concat [["npm" "install"]]
                                      (mapv (fn [build] ["npx" "shadow-cljs" "compile" (str build)])
                                            (build-frontend-compiler/builds app-dir))
                                      ;;happe"../automaton_web"
                                      [["npx" "karma" "start" "--single-run"]]))))
      (build-log/fatal "Tests have failed")
      (System/exit build-exit-codes/catch-all))))
