(ns automaton-build-app.tasks.container-list
  (:require [automaton-build-app.containers.local-engine :as build-local-engine]
            [automaton-build-app.os.terminal-msg :as build-terminal-msg]))

(defn container-list
  "List all available containers"
  [_cli-opts _app _bb-edn-args]
  (apply build-terminal-msg/println-msg (build-local-engine/container-image-list)))
