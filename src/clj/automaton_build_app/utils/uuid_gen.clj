(ns automaton-build-app.utils.uuid-gen "Generate uuid, is a proxy to `java.util.UUID`")

(defn time-based-uuid "Generate a time based uuid, so sorting uuid is sorting chronologically" [] (java.util.UUID/randomUUID))
