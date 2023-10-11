(ns automaton-build-app.code-helpers.update-deps
  "Update the dependencies of the project (both clj and cljs compilers)"
  (:require [antq.core]))

(defn do-update
  "Update the depenencies"
  [_opts]
  (antq.core/-main "--upgrade" "--exclude=cider/cider-nrepl" "--exclude=refactor-nrepl/refactor-nrepl"))
