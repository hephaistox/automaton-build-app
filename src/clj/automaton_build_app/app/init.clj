(ns automaton-build-app.app.init
  (:require [automaton-build-app.tasks.launcher.cli-opts :as build-tasks-cli-opts]
            [clojure.pprint :as pp]))

(defn init!
  "The first things to do during app startup
  It contains log and build the application data (with bb.edn, build_config, and deps.edn)

  The order is:
  * first, bb pp/simple-dispatch init to improve log message during bb errors
     * as it is agnostic and some errors may occur in the next functions
  * second, a first iteration of cli-opts call, to enable common tasks
     *  as it will initiate logs

  Returns nil
  Params:
  * none"
  []
  (prefer-method pp/simple-dispatch clojure.lang.IPersistentMap clojure.lang.IDeref)
  (build-tasks-cli-opts/cli-opts {} *command-line-args*)
  nil)
