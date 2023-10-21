(ns automaton-build-app.schema
  "Validate the data against the schema.
  Is a proxy for malli"
  (:require [automaton-build-app.log :as build-log]
            [malli.core :as m]
            [malli.error :as me]))

(defn valid?
  "Return true if the data is not matching the schema
  Params:
  * `schema` schema to match
  * `data` data to check appliance to schema"
  [schema data]
  (if (m/validate schema data)
    data
    (do (build-log/warn-format "Not validated %s, data was `%s`"
                               (-> schema
                                   (m/explain data)
                                   (me/humanize))
                               (pr-str data))
        nil)))
