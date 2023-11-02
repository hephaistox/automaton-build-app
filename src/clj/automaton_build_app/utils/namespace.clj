(ns automaton-build-app.utils.namespace
  "Helpers for namespace"
  (:require [clojure.string :as str]
            [automaton-build-app.os.files :as build-files]))

(defn update-last [& v] (let [v (vec v)] (when-not (empty? v) (conj (pop v) (str (last v) ".clj")))))

(defn ns-to-file
  "Transform a symbol of a namespace to its filename"
  [ns]
  (->> (-> (name ns)
           (str/replace #"-" "_")
           (str/split #"\."))
       (apply update-last)
       (apply build-files/create-file-path)))
