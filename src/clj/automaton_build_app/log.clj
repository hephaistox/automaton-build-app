(ns automaton-build-app.log
  "Logger for building system
  Is a basic implementation with print

  That log is disabled during test, which is detected with `hephaistox-in-test`"
  (:require [automaton-build-app.utils.string :as build-string]
            [clojure.pprint :as pp]
            [automaton-build-app.os.java-properties :as build-java-properties]))

(def size-command
  "Size of the command line to be managed, measured on mcbook pro"
  2430)

(def ^:private log-levels [:trace :debug :info :warning :error :fatal])

(def min-level "Minimum level to be displayed during log" (atom -1))

(defn set-min-level!
  "Set the minimum level"
  [min-level*]
  (reset! min-level (.indexOf log-levels min-level*)))

(defn min-level-kw [] (get log-levels @min-level))

(defmacro print-message
  "Helper function to print the log message"
  [level & messages]
  `(when-not (build-java-properties/get-java-property "hephaistox-in-test")
     (let [prefix# (str (.format (java.text.SimpleDateFormat. "HH:mm:ss:SSS")
                                 (java.util.Date.))
                        " "
                        ~level
                        "-"
                        (-> (ns-name *ns*)
                            str
                            (build-string/fix-length 45 "" " "))
                        "--> ")
           suffix# ""]
       (println (build-string/limit-length (str ~@messages)
                                           size-command
                                           prefix#
                                           suffix#
                                           (constantly nil))))))

(defmacro trace
  [& messages]
  `(when (>= 0 @min-level) (print-message "T" ~@messages)))

(defmacro debug
  [& messages]
  `(when (>= 1 @min-level) (print-message "D" ~@messages)))

(defmacro info
  [& messages]
  `(when (>= 2 @min-level) (print-message "I" ~@messages)))

(defmacro warn
  [& messages]
  `(when (>= 3 @min-level) (print-message "W" ~@messages)))

(defmacro error
  [& messages]
  `(when (>= 4 @min-level) (print-message "E" ~@messages)))

(defmacro fatal
  [& messages]
  `(when (>= 5 @min-level) (print-message "F" ~@messages)))

(defmacro trace-format
  [fmt & messages]
  `(when (>= 0 @min-level) (print-message "T" (format ~fmt ~@messages))))

(defmacro debug-format
  [fmt & messages]
  `(when (>= 1 @min-level) (print-message "D" (format ~fmt ~@messages))))

(defmacro info-format
  [fmt & messages]
  `(when (>= 2 @min-level) (print-message "I" (format ~fmt ~@messages))))

(defmacro warn-format
  [fmt & messages]
  `(when (>= 3 @min-level) (print-message "W" (format ~fmt ~@messages))))

(defmacro error-format
  [fmt & messages]
  `(when (>= 4 @min-level) (print-message "E" (format ~fmt ~@messages))))

(defmacro fatal-format
  [fmt & messages]
  `(when (>= 5 @min-level) (print-message "F" (format ~fmt ~@messages))))

(defmacro trace-exception
  [e]
  `(when (>= 0 @min-level) (print-message "T" (pr-str ~e))))

(defmacro debug-exception
  [e]
  `(when (>= 1 @min-level) (print-message "D" (pr-str ~e))))

(defmacro info-exception
  [e]
  `(when (>= 2 @min-level) (print-message "I" (pr-str ~e))))

(defmacro warn-exception
  [e]
  `(when (>= 3 @min-level) (print-message "W" (pr-str ~e))))

(defmacro error-exception
  [e]
  `(when (>= 4 @min-level) (print-message "E" (pr-str ~e))))

(defmacro fatal-exception
  [e]
  `(when (>= 5 @min-level) (print-message "F" (pr-str ~e))))

(defmacro trace-vars
  [msg & variables]
  `(when (>= 0 @min-level)
     (print-message "T" ~msg)
     (print-message "T"
                    (apply hash-map
                      (interleave (map symbol [~@variables])
                                  (map var-get [~@variables]))))))

(defmacro trace-map
  [msg & variables]
  `(when (>= 0 @min-level)
     (print-message "T" ~msg)
     (print-message "T" (pp/pprint (apply hash-map [~@variables])))))

(defmacro trace-data
  [data & messages]
  `(when (>= 0 @min-level)
     (when-not (empty? ~messages) (print-message "T" ~@messages))
     (print-message "T" (pr-str ~data))))

(defmacro warn-data
  [data & messages]
  `(when (>= 3 @min-level)
     (when-not (empty? ~messages) (print-message "W" ~@messages))
     (print-message "W" (pr-str ~data))))

(defmacro error-data
  [data & messages]
  `(when (>= 4 @min-level)
     (when-not (empty? ~messages) (print-message "E" ~@messages))
     (print-message "E" (pr-str ~data))))
