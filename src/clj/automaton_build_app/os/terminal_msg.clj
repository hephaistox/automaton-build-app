(ns automaton-build-app.os.terminal-msg
  "Print message on the terminal - even if the log is activated and routed somewhere else.
  So all low level messages, like fatal, like pipeable messages should use this")

(defn println-msg "Display a regular message on the terminal" [& msg] (apply println msg))
