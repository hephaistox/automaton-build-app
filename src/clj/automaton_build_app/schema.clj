(ns automaton-build-app.schema
  "Validate the data against the schema.
  Is a proxy for malli"
  (:require [automaton-build-app.log :as build-log]
            [malli.core :as malli]
            [malli.error :as malli-error]))

(defn valid?
  "Returns data if the data is not matching the schema
  Return nil otherwise
  Params:
  * `schema` schema to match
  * `data` data to check appliance to schema"
  [schema data]
  (if (malli/validate schema data)
    data
    (do (build-log/warn-format "Not validated %s"
                               (-> schema
                                   (malli/explain data)
                                   (malli-error/humanize)))
        (build-log/warn-data (pr-str data))
        nil)))
