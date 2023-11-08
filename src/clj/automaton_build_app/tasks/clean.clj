(ns automaton-build-app.tasks.clean
  (:require [automaton-build-app.os.files :as build-files]
            [automaton-build-app.log :as build-log]))

(defn clean
  "Clean cache files for compilers to start from scratch"
  [_parsed-cli-opts _app-dir app-data _bb-edn-args]
  (let [dirs (get-in app-data [:clean :compile-logs-dirs])]
    (build-log/debug-format "The directories `%s` are cleaned" dirs)
    (build-files/delete-files dirs)))
