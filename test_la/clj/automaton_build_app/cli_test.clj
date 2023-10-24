(ns automaton-build-app.cli-test
  "Test all the cli that they returns `0` exit code"
  (:require
   [automaton-build-app.log :as build-log]
   [automaton-build-app.os.commands :as build-cmds]
   [automaton-build-app.os.exit-codes :as exit-codes]))

(def cmds-to-test
  "List of commands to test"
  [{:cmd ["bb" "blog"]
    :cmd-name "blog"}
   {:cmd ["bb" "clean"]
    :cmd-name "clean"}
   {:cmd ["bb" "clean-hard" {:in "q"}]
    :cmd-name "clean-hard"}
   {:cmd ["bb" "code-doc"]
    :cmd-name "code-doc"}
   {:cmd ["bb" "compile-to-jar"]
    :cmd-name "compile-to-jar"}
   {:cmd ["bb" "container-list"]
    :cmd-name "container-list"}
   {:cmd ["bb" "container-clear"]
    :cmd-name "container-clear"}
   {:cmd ["bb" "gha"]
    :cmd-name "gha"
    :expected-exit-code 1}
   {:cmd ["bb" "gha" "-f" :cmd-name "blog"]}
   {:cmd ["bb" "gha-lconnect" {:in "exit\n"}]
    :cmd-name "gha-lconnect"
    :skip? true}
   {:cmd ["bb" "lconnect"]
    :cmd-name "lconnect"
    :skip? true}
   {:cmd ["bb" "la"]
    :cmd-name "la"
    :skip? true}
   {:cmd ["bb" "ltest"]
    :cmd-name "ltest"}
   {:cmd ["bb" "publish" "-t" "v-test"]
    :cmd-name "publish"
    :skip? true}
   {:cmd ["bb" "push" "-m" "la" "-t" "la"]
    :cmd-name "push"
    :skip? true}
   {:cmd ["bb" "report"]
    :cmd-name "report"}
   {:cmd ["bb" "updated-deps"]
    :cmd-name "updated-deps"
    :skip? true}])

(defn select-tasks
  [selected-tasks cmds-to-test]
  (let [selected-tasks (set selected-tasks)
        selected-cmds (filter #(contains?  selected-tasks
                                           (:cmd-name %))
                              cmds-to-test)]
    (when (= (count selected-cmds)
             (count selected-tasks))
      (build-log/warn-format "Mismatch in tasks, build_config %s, bb.edn %s"
                             (count selected-tasks)
                             (count selected-cmds)))
    selected-cmds))

(defn- run-cmd
  "Run a command
  Params:
  * `cmd` command to execute
  * `expanded-cmd` command ready to display
  * `expected-exit-code` exit code that the cmd should returned"
  [cmd expanded-cmd expected-exit-code]
  (let [[[exit-code msg]] (build-cmds/execute-and-trace-return-exit-codes cmd)]
    (build-log/info msg)
    (if (= expected-exit-code
           exit-code)
      [true #(build-log/info-format "Test `%s` successfully passed" expanded-cmd)]
      [false #(build-log/error-format "Test `%s` expects `%s` and found `%s`"
                                      expanded-cmd
                                      expected-exit-code
                                      exit-code)])))

(defn- test-cli-cmd
  "Run the command, print the message
  Params:
  * `map-cmd` map of the command to execute"
  [{:keys [cmd expected-exit-code skip?]
    :or {expected-exit-code exit-codes/ok}
    :as _map-cmd}]
  (let [expanded-cmd (build-cmds/expand-cmd cmd)]
    (if skip?
      [true #(build-log/warn-format "Skip `%s` " expanded-cmd)]
      (do
        (build-log/info-format "Test cmd `%s`:" expanded-cmd)
        (run-cmd cmd expanded-cmd expected-exit-code)))))

(defn- exec-and-return
  "In a command result, execute the display-return-fn and return the value"
  [[passed? display-return-fn]]
  (display-return-fn)
  [passed? display-return-fn])

(defn cli-test
  "Test to execute
  Params:
  * `cmds-to-test`"
  [cmds-to-test]
  (let [results (mapv (comp exec-and-return
                            test-cli-cmd)
                      cmds-to-test)]
    (build-log/info "Summary")
    (doseq [[_ display-return-fn] results]
      (display-return-fn))
    (if (every? first
                results)
      (build-log/info "All tests passed")
      (do
        (build-log/error "Errors found")
        (System/exit exit-codes/catch-all)))))

(comment
  (cli-test cmds-to-test)
;
  )
