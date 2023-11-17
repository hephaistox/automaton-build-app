(ns automaton-build-app.doc.vizualise-deps
  "Build the dependencies
  Proxy to tools deps"
  (:require [automaton-build-app.log :as build-log]
            [automaton-build-app.os.files :as build-files]))

(defn vizualize-deps
  "Vizualize the dependencies between deps"
  [output-filename]
  (build-log/info "Graph of deps")
  (build-log/trace-format "Graph stored in `%s`" output-filename)
  (build-files/create-parent-dirs output-filename)
  (build-log/warn "Skip as it is not working yet")
  #_(build-cmds/execute-and-trace ["clj" "-Tgraph" "graph" ":output" output-filename])
  true)
