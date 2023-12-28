(ns automaton-build-app.tasks.lbe-repl
  (:require [babashka.process :as babashka-process]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.os.exit-codes :as build-exit-codes]))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn exec
  [_task-map {:keys [repl-aliases]}]
  (try (future (babashka-process/shell "clojure" (apply str "-M:" repl-aliases)))
       build-exit-codes/ok
       (catch Exception e (build-log/error-exception e) build-exit-codes/cannot-execute)))
