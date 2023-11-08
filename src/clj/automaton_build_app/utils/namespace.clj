(ns automaton-build-app.utils.namespace
  "Helpers for namespace"
  (:require [clojure.string :as str]
            [automaton-build-app.os.files :as build-files]
            [automaton-build-app.log :as build-log]))

(defn update-last [& v] (let [v (vec v)] (when-not (empty? v) (conj (pop v) (str (last v) ".clj")))))

(defn ns-to-file
  "Transform a symbol of a namespace to its filename"
  [ns]
  (->> (-> (name ns)
           (str/replace #"-" "_")
           (str/split #"\."))
       (apply update-last)
       (apply build-files/create-file-path)))

(defn require-ns
  "Require the namespace of the body-fn
  Params:
  * `f` is a function full qualified symbol. It could be a string"
  [f]
  (some-> f
          symbol
          namespace
          symbol
          require))

(defn qualified-name
  "Return the qualified name of a function"
  [s]
  (-> (apply str (interpose "/" ((juxt namespace name) (symbol s))))))

(defn symbol-to-fn-call
  "Resolve the symbol and execute the associated function"
  [f & args]
  (require-ns f)
  (if-let [resolved-task-fn (some-> f
                                    resolve)]
    (apply resolved-task-fn args)
    (build-log/warn-format "No valid function passed, (i.e %s)" f)))
