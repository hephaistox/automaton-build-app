(ns automaton-build-app.configuration.simple-files
  "Simple configuration based on files
  Data can be set in java property `heph-conf`, if more files are needed you can pass them separated by `,`"
  (:require [automaton-build-app.configuration.edn-read :as build-conf-edn-read]
            [clojure.edn :as edn]
            [automaton-build-app.configuration.protocol :as build-conf-prot]
            [automaton-build-app.os.java-properties :as build-java-properties]
            [automaton-build-app.utils.keyword :as build-utils-keyword]))

(defrecord SimpleConf [config-map]
  build-conf-prot/Conf
    (read-conf-param [_this key-path] (get-in config-map key-path)))

(def default-config-files ["env/development/config.edn" "env/common_config.edn"])

(defn parse-number [^String v] (try (Long/parseLong v) (catch NumberFormatException _ (BigInteger. v)) (catch Exception _ v)))

(defn str->value
  "ENV vars and system properties are strings. str->value will convert:
   the numbers to longs, the alphanumeric values to strings, and will use Clojure reader for the rest
   in case reader can't read OR it reads a symbol, the value will be returned as is (a string)"
  [v]
  (cond (re-matches #"[0-9]+" v) (parse-number v)
        (re-matches #"^(true|false)$" v) (Boolean/parseBoolean v)
        (re-matches #"\w+" v) v
        :else (try (let [parsed (edn/read-string v)] (if (symbol? parsed) v parsed)) (catch Exception _ v))))

(defn read-system-env
  []
  (->> (System/getenv)
       (map (fn [[k v]] [(build-utils-keyword/keywordize k) (str->value v)]))
       (into {})))

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
                        (apply merge (read-system-env)))]
    (->SimpleConf config-map)))
