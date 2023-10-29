(ns automaton-build-app.cli-test
  "Test all the cli that they returns `0` exit code"
  (:require [automaton-build-app.log :as build-log]
            [automaton-build-app.os.commands :as build-cmds]
            [automaton-build-app.bb-tasks :as build-bb-tasks]
            [automaton-build-app.os.exit-codes :as exit-codes]))

(defn select-tasks
  "Select the tasks executed by a cli - as each app may have its varians
  Params:
  * `selected-tasks` set of tasks name that are selected (must match `cmd-name` argument)
  * `cmds-to-test` set of tasks to execute"
  [selected-tasks cmds-to-test]
  (let [selected-tasks (set selected-tasks)
        selected-cmds (filter #(contains? selected-tasks (:cmd-name %))
                        cmds-to-test)]
    (when (= (count selected-cmds) (count selected-tasks))
      (build-log/warn-format "Mismatch in tasks, build_config %s, bb.edn %s"
                             (count selected-tasks)
                             (count selected-cmds)))
    selected-cmds))

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
        [[exit-code _]] (build-cmds/execute-and-trace-return-exit-codes
                          cmd-with-args)]
    (if (= expected-exit-code exit-code)
      [true
       #(build-log/info-format "Test `%s` successfully passed" expanded-cmd)]
      [false
       #(build-log/error-format "Test `%s` expects `%s` and found `%s`"
                                expanded-cmd
                                expected-exit-code
                                exit-code)])))

(defn- test-cli-cmd
  "Run the command, print the message
  Params:
  * `cmd-line-args` command line arguments
  * `map-cmd` map of the command to execute"
  [cmd-line-args
   [_task-name
    {:keys [la-test expected-exit-code skip? process-opts],
     :or {expected-exit-code exit-codes/ok},
     :as _map-cmd}]]
  (let [cmd (get la-test :cmd)
        expanded-cmd (build-cmds/expand-cmd cmd)]
    (if skip?
      [true #(build-log/warn-format "Skip `%s` " expanded-cmd)]
      (do (build-log/info-format "Test cmd `%s`:" expanded-cmd)
          (run-cmd cmd
                   expanded-cmd
                   expected-exit-code
                   cmd-line-args
                   process-opts)))))

(defn- exec-and-return
  "In a command result, execute the display-return-fn and return the value
  Params:
  * ``"
  [[passed? display-return-fn]]
  (display-return-fn)
  [passed? display-return-fn])

(defn cli-test
  "Test to execute
  Params:
  * `cmds-to-test` collection of maps defining the tasks to execute (should comply to automaton-build-app.bb-tasks/registry-schema)
  * `cli-args` arguments of the cli - useful to keep that setup in the called bb tasks"
  [cmds-to-test cli-args]
  (let [results (mapv (comp exec-and-return (partial test-cli-cmd cli-args))
                  cmds-to-test)]
    (build-log/info "Summary")
    (doseq [[_ display-return-fn] results] (display-return-fn))
    (if (every? first results)
      (build-log/info "All tests passed")
      (do (build-log/error "Errors found")
          (System/exit exit-codes/catch-all)))))

(comment
  (cli-test build-bb-tasks/registry {})
  ;
)
