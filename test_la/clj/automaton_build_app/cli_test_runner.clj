(ns automaton-build-app.cli-test-runner
  "Test all the cli that they returns `0` exit code"
  (:require [automaton-build-app.log :as build-log]
            [automaton-build-app.os.commands :as build-cmds]
            [automaton-build-app.os.exit-codes :as build-exit-codes]))

(defn- run-cmd
  "Run a command
  Params:
  * `cmd` command to execute
  * `expanded-cmd` command ready to display
  * `expected-exit-code` exit code that the cmd should returned
  * `cmd-line-args` command line arguments
  * `process-opts` options to pass to process creation"
  [cmd expanded-cmd expected-exit-code cmd-line-args process-opts]
  (let [cmd-with-args (concat cmd cmd-line-args [process-opts])
        [[exit-code _]] (build-cmds/execute-and-trace-return-exit-codes cmd-with-args)]
    (if (= expected-exit-code exit-code)
      [true #(build-log/info-format "Test `%s` successfully passed" expanded-cmd)]
      [false #(build-log/error-format "Test `%s` expects `%s` and found `%s`" expanded-cmd expected-exit-code exit-code)])))

(defn- test-cli-cmd
  "Run the command, print the message
  Params:
  * `cmd-line-args` command line arguments
  * `map-cmd` map of the command to execute"
  [cmd-line-args
   [_task-name
    {:keys [la-test]
     :as _map-cmd}]]
  (build-log/trace "Test is la-test" la-test)
  (let [{:keys [skip? process-opts cmd expected-exit-code]
         :or {expected-exit-code build-exit-codes/ok}}
        la-test
        expanded-cmd (build-cmds/expand-cmd cmd)]
    (if (or (empty? la-test) skip?)
      [true #(build-log/warn-format "Skip `%s` " expanded-cmd)]
      (do (build-log/info-format "Test cmd `%s`:" expanded-cmd) (run-cmd cmd expanded-cmd expected-exit-code cmd-line-args process-opts)))))

(defn- exec-and-return
  "In a command result, execute the display-return-fn and return the value
  Params:
  * "
  [[passed? display-return-fn]]
  (display-return-fn)
  [passed? display-return-fn])

(defn cli-test
  "Test cli commands
  Params:
  * `task-registry`
  * `cli-args` arguments of the cli - useful to keep that setup in the called bb tasks"
  [task-registry cli-args]
  (let [results (->> task-registry
                     (filter (fn [[_ task-map]] (:la-test task-map)))
                     (mapv (comp exec-and-return (partial test-cli-cmd cli-args))))]
    (build-log/info "Summary")
    (doseq [[_ returned-value-fn] results] (returned-value-fn))
    (if (every? first results)
      (build-log/info "All tests passed")
      (do (build-log/error "Errors found") (System/exit build-exit-codes/catch-all)))))
