(ns automaton-build-app.configuration.simple-files
  "Simple configuration based on files
  Data can be set in java property `heph-conf`, if more files are needed you can pass them separated by `,`"
  (:require [automaton-build-app.configuration.edn-read :as build-conf-edn-read]
            [automaton-build-app.configuration.protocol :as build-conf-prot]
            [automaton-build-app.os.java-properties :as build-java-properties]))

(defrecord SimpleConf [config-map]
  build-conf-prot/Conf
    (read-conf-param [_this key-path] (get-in config-map key-path)))

(def default-config-files ["env/development/config.edn" "env/common_config.edn"])

(defn property->config-files
  "Turn java property into sequence of config file paths"
  [property-name]
  (some-> property-name
          build-java-properties/get-java-property
          build-java-properties/split-property-value))

(defn ensure-config-files
  "This is done in case the project does not have access to jvm-opts. E.g. when tasks are from bb.edn"
  [config-files]
  (if-not (and (nil? config-files) (empty? config-files)) config-files default-config-files))

(defn make-simple-conf
  "Create the simple configuration"
  []
  (let [config-map (->> "heph-conf"
                        property->config-files
                        ensure-config-files
                        (mapv build-conf-edn-read/read-edn)
                        (filterv some?)
                        (apply merge))]
    (->SimpleConf config-map)))
