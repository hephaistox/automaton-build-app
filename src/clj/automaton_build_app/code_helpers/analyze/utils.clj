(ns automaton-build-app.code-helpers.analyze.utils
  "Helpers function to manage analyze of code"
  (:require [automaton-build-app.log :as build-log]
            [automaton-build-app.os.files :as build-files]
            [automaton-build-app.os.edn-utils :as build-edn-utils]))

(defn- matches-to-output-lines [match-to-output-line matches] (mapv match-to-output-line matches))

(defn save-report
  "Matches
  Params:
  * `matches`
  * `report-title`
  * `filename`"
  [matches report-title filename match-to-output-line]
  (if (empty? matches)
    (build-files/delete-files [filename])
    (do (build-log/info report-title)
        (build-edn-utils/spit-edn filename
                                  (->> matches
                                       (matches-to-output-lines match-to-output-line))
                                  report-title)
        matches)))

(defn assert-empty
  [matches filename assert-message]
  (when-not (empty? matches) (build-log/warn-data matches (format "%s - open file `%s` for details" assert-message filename)) true))
