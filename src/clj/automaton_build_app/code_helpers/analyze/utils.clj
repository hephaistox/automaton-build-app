(ns automaton-build-app.code-helpers.analyze.utils
  "Helpers function to manage analyze of code"
  (:require [automaton-build-app.log :as build-log]
            [automaton-build-app.os.exit-codes :as build-exit-codes]
            [automaton-build-app.os.files :as build-files]
            [clojure.string :as str]))

(defn- matches-to-output-lines
  [match-to-output-line matches]
  (map match-to-output-line matches))

(defn save-report
  "Matches
  Params:
  * `matches`
  * `report-title`
  * `filename`"
  [matches report-title filename match-to-output-line]
  (build-log/info report-title)
  (let [report-content (->> matches
                            (matches-to-output-lines match-to-output-line)
                            (concat [report-title])
                            (str/join "\n")
                            print-str)]
    (build-files/spit-file filename report-content)
    matches))

(defn assert-empty
  [matches assert-message]
  (when-not (empty? matches) (build-log/warn-data matches assert-message))
  (System/exit build-exit-codes/rules-broken))

