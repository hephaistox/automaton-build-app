(ns automaton-build-app.tasks.error
  (:require [automaton-build-app.log :as build-log]
            [automaton-build-app.os.exit-codes :as build-exit-codes]))

(defn error [_cli-opts _app _bb_edn_args] (build-log/fatal "This error is intentional") (System/exit build-exit-codes/catch-all))
