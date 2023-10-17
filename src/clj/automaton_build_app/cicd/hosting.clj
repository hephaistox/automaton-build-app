(ns automaton-build-app.cicd.hosting
  "Manage hosting on clever cloud
  Proxy to clever cli tool"
  (:require
   [automaton-build-app.os.commands :as build-cmd]))

(defn hosting-installed?*
  "Check clever cloud is useable
  Params:
  * `cc-command` (Optional, default clever) name of the command for clever cloud"
  ([cc-command]
   (every? string?
           (build-cmd/execute-get-string [cc-command "version" {:dir "."}])))
  ([]
   (hosting-installed?* "clever")))

(def hosting-installed?
  "Check clever cloud is useable"
  hosting-installed?*)

(defn prod-ssh
  "Connect to the production server
  Params:
  * `dir` the root directory where `clever` cli json is stored"
  [dir]
  (hosting-installed?)
  (build-cmd/execute-and-trace ["clever" "ssh" {:dir dir}]))

(defn upsert-cc-app
  "Not implemented yet
  Params:
  * `app-name` application name in clever cloud
  * `dir` directory where to execute"
  [app-name dir]
  (hosting-installed?)
  (build-cmd/execute-and-trace ["clever" "create" "--type" "docker" "--org" "Hephaistox" "--region" "par" app-name
                                {:dir dir}]))
