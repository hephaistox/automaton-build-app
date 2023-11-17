(ns automaton-build-app.doc.vizualise-ns
  "Vizualise namespaces
  Proxy to io.dominic.vizns.core"
  (:require [automaton-build-app.log :as build-log]
            [automaton-build-app.os.files :as build-files]
            [io.dominic.vizns.core :as vizns]))

(defn vizualize-ns
  "Vizualise all namespaces relations"
  [deps-filename]
  (build-log/info "Graph of ns - deps link")
  (build-log/trace-format "Graph stored in `%s`" deps-filename)
  (build-files/create-parent-dirs deps-filename)
  (try (vizns/-main "single" "-o" deps-filename "-f" "svg")
       true
       (catch Exception e (build-log/error "Unexpected error during execution of vizns") (build-log/trace-exception e) false)))
