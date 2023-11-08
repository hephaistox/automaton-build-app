(ns automaton-build-app.repl
  "REPL component
  Design decision:
  * The repl could have pushed to dev, but leaving it here allows to remotely connect to the remote repl, like la or production"
  (:require [clojure.core.async :refer [<!! chan]]))

(defn -main
  "Entry point for simple / emergency repl"
  [& _args]
  (let [c (chan)]
    (require '[automaton-build-app.repl.launcher])
    #_{:clj-kondo/ignore [:unresolved-namespace]}
    ((resolve 'automaton-build-app.repl.launcher/start-repl))
    (<!! c)))
