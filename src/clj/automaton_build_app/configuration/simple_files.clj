(ns automaton-build-app.configuration.simple-files
  "Simple configuration based on files
  Data can be set in two files:
  * one set in `heph-common-conf` java property for all values not specific to an environment
  * one set in `heph-conf` java property for all values specific to that environment"
  (:require [automaton-build-app.os.java-properties :as build-java-properties]
            [automaton-build-app.configuration.edn-read :as build-conf-edn-read]
            [automaton-build-app.configuration.protocol :as build-conf-prot]))

(defrecord SimpleConf [config-map]
  build-conf-prot/Conf
    (read-conf-param [_this key-path] (get-in config-map key-path)))

(defn- load-conf
  [property-name]
  (some-> (build-java-properties/get-java-property property-name)
          build-conf-edn-read/read-edn))

(defn make-simple-conf
  "Create the simple configuration"
  []
  (let [config-map (->> ["heph-conf" "heph-common-conf"]
                        (mapv load-conf)
                        (filterv some?)
                        (apply merge))]
    (->SimpleConf config-map)))
