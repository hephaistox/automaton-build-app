(ns automaton-build-app.tasks.launcher.launch-on-clj-env
  (:require [automaton-build-app.log :as build-log]
            [automaton-build-app.os.commands :as build-cmds]
            [automaton-build-app.os.exit-codes :as build-exit-codes]
            [automaton-build-app.os.edn-utils :as build-edn-utils]))

(defn switch-to-clj
  "Run the `task-fn` on a clojure environment"
  [task-fn cli-opts app bb-edn-args]
  (let [clj-input-tmp-file (build-edn-utils/create-tmp-edn "to-clj-x.edn")]
    (build-edn-utils/spit-edn clj-input-tmp-file
                              {:cli-opts cli-opts
                               :app app
                               :bb-edn-args bb-edn-args
                               :task-fn task-fn})
    (build-log/trace-format "Data passed to file `%s` to launch clojure" clj-input-tmp-file)
    (when-not (build-cmds/execute-and-trace (vector "clojure"
                                                    "-X:build:bb-deps"
                                                    (str 'automaton-build-app.tasks.launcher.launch-on-clj-entry-point/entry-point)
                                                    :clj-input-tmp-file (format "\"%s\"" clj-input-tmp-file)))
      (build-log/trace "The clj command has failed, so the exit code is passed to the bb")
      (System/exit build-exit-codes/catch-all))))

(comment
  (switch-to-clj 'automaton-build-app.tasks.code-doc/code-doc {:app-dir ""} {} {})
  ;
)
