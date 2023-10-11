(ns automaton-build-app.log
  "Logger for building system
  Is a basic implementation with print

  That log is disabled during test, which is detected with `hephaistox-in-test`"
  (:require
   [automaton-build-app.utils.string :as build-string]
   [automaton-build-app.os.java-properties :as build-java-properties]))

(def size-command
  "Size of the command line to be managed, measured on mcbook pro"
  185)

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
                            (build-string/fix-length 45
                                                     "" " "))
                        "--> ")
           suffix# ""]
       (println (build-string/limit-length (str ~@messages)
                                           size-command
                                           prefix#
                                           suffix#
                                           (constantly nil))))))

(defmacro trace
  [& messages]
  `(print-message "T" ~@messages))

(defmacro debug
  [& messages]
  `(print-message "D" ~@messages))

(defmacro info
  [& messages]
  `(print-message "I" ~@messages))

(defmacro warn
  [& messages]
  `(print-message "W" ~@messages))

(defmacro error
  [& messages]
  `(print-message "E" ~@messages))

(defmacro fatal
  [& messages]
  `(print-message "F" ~@messages))

(defmacro trace-format
  [fmt & messages]
  `(print-message "T" (format ~fmt ~@messages)))

(defmacro debug-format
  [fmt & messages]
  `(print-message "D" (format ~fmt ~@messages)))

(defmacro info-format
  [fmt & messages]
  `(print-message "I" (format ~fmt ~@messages)))

(defmacro warn-format
  [fmt & messages]
  `(print-message "W" (format ~fmt ~@messages)))

(defmacro error-format
  [fmt & messages]
  `(print-message "E" (format ~fmt ~@messages)))

(defmacro fatal-format
  [fmt & messages]
  `(print-message "F" (format ~fmt ~@messages)))

(defmacro trace-exception
  [e]
  `(print-message "T" (pr-str ~e)))

(defmacro debug-exception
  [e]
  `(print-message "D" (pr-str ~e)))

(defmacro info-exception
  [e]
  `(print-message "I" (pr-str ~e)))

(defmacro warn-exception
  [e]
  `(print-message "W" (pr-str ~e)))

(defmacro error-exception
  [e]
  `(print-message "E" (pr-str ~e)))

(defmacro fatal-exception
  [e]
  `(print-message "F" (pr-str ~e)))
