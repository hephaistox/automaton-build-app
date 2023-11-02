(ns automaton-build-app.code-helpers.update-deps-clj
  (:require [antq.core]))

(defn do-update
  "Update the depenencies"
  [dir]
  (antq.core/-main "--upgrade" (format "--directory=%s" dir) "--exclude=cider/cider-nrepl" "--exclude=refactor-nrepl/refactor-nrepl"))
