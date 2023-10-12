(ns automaton-build-app.cicd.version
  "Version of the current codebase"
  (:require
   [automaton-build-app.apps.app :as build-app]
   [clojure.tools.build.api :as build-build-api]))

(defn version-to-push
  []
  (let [{:keys [publication]} (build-app/build-app-data "")
        {:keys [major-version]} publication]
    (->> (build-build-api/git-count-revs nil)
         (format major-version ))))
