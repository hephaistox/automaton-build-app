(ns automaton-build-app.os.java-properties "Adapter to java properties")

(defn get-java-properties "Get the java properties" [] (into {} (System/getProperties)))

(defn get-java-property
  "Get a java property named `property-name`, if not found return `default-value`"
  ([property-name default-value] (or (System/getProperty property-name) default-value))
  ([property-name] (System/getProperty property-name)))
