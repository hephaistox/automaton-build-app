(ns automaton-build-app.os.js-config
  "Everything about config.js files manipulation"
  (:require [automaton-build-app.os.files :as build-files]
            [clojure.string :as str]))

(defn join-config-items
  "Joins config items (like prestes requires, content paths etc.). Any items in a way that is acceptable by js config files"
  [config-items]
  (str/join "," config-items))

(defn js-require "Turns `package-name` into js require" [package-name] (str "require('" package-name "')"))

(defn load-js-config [filepath] (when (build-files/is-existing-file? filepath) (build-files/read-file filepath)))

(defn write-js-config [filepath content] (build-files/spit-file filepath content))
