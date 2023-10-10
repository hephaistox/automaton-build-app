(ns automaton-build-app.tasks.common
  "Helpers for managing tasks")

(defn exit-code
  "Call appropriate system exit code depending on the result of a command"
  [[cmd-exit-code cmd-message :as _cmd-res]]
  (when-not (zero? cmd-exit-code)
    (println (format "Exit on error: code (%s) - %s" cmd-exit-code cmd-message))
    (System/exit cmd-exit-code)))
