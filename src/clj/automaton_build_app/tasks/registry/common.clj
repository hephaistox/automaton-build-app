(ns automaton-build-app.tasks.registry.common "Data for the common task registry")

(defn tasks
  []
  {'clean {:doc "Clean cache files for compiles, and logs"
           :la-test {:cmd ["bb" "clean"]}
           :task-fn 'automaton-build-app.tasks.clean/clean}
   'clean-hard
   {:doc
    "Clean all files which are not under version control (it doesn't remove untracked file or staged files if there are eligible to `git add .`)"
    :la-test {:cmd ["bb" "clean-hard"]
              :process-opts {:in "q"}}
    :task-fn 'automaton-build-app.tasks.clean-hard/clean-hard}
   'code-doc {:doc "Create the documentation of the app"
              :la-test {:cmd ["bb" "code-doc"]}
              :pf :clj
              :task-fn 'automaton-build-app.tasks.code-doc/code-doc}
   'compile-to-jar {:doc "Compile that code to a jar"
                    :pf :clj
                    :la-test {:cmd ["bb" "compile-to-jar"]}
                    :task-fn 'automaton-build-app.tasks.compile-to-jar/compile-to-jar}
   'container-clear {:doc "Clear all local containers"
                     :la-test {:cmd ["bb" "container-clear"]
                               :skip? true}
                     :task-fn 'automaton-build-app.tasks.container-clear/container-clear}
   'container-list {:doc "List all available containers"
                    :la-test {:cmd ["bb" "container-list"]}
                    :task-fn 'automaton-build-app.tasks.container-list/container-list}
   'format {:doc "Format the whole documentation"
            :la-test {:cmd ["bb" "format"]}
            :task-fn 'automaton-build-app.tasks.format-files/format-files}
   'gha {:doc "Github action command - launched is automatically by github"
         :la-test {:cmd ["bb" "gha" "-f"]
                   :skip? true}
         :specific-cli-opts-kws [:force]
         :pf :clj
         :task-fn 'automaton-build-app.tasks.gha/gha}
   'gha-container-publish {:doc "Update the gha container to run that app"
                           :la-test {:cmd ["bb" "gha-container-publish"]
                                     :skip? true}
                           :specific-cli-opts-kws [:tag]
                           :task-fn 'automaton-build-app.tasks.gha-container-publish/gha-container-publish}
   'gha-lconnect {:doc "Connect to a local container running this code"
                  :la-test {:cmd ["bb" "gha-lconnect"]
                            :skip? true
                            :process-opts {:in "exit\n"}}
                  :task-fn 'automaton-build-app.tasks.gha-lconnect/gha-lconnect}
   'la {:doc "Local acceptance test"
        :la-test {:cmd ["bb" "la"]
                  :skip? true}
        :task-fn 'automaton-build-app.tasks.la/la}
   'lconnect {:doc "Local connect - repl"
              :la-test {:cmd ["bb" "lconnect"]
                        :skip? true}
              :task-fn 'automaton-build-app.tasks.lconnect/lconnect}
   'lint {:doc "linter"
          :la-test {:cmd ["bb" "lint"]}
          :task-fn 'automaton-build-app.tasks.lint/lint}
   'ltest {:doc "Local test"
           :la-test {:cmd ["bb" "ltest"]}
           :task-fn 'automaton-build-app.tasks.ltest/ltest}
   'push-local-dir-to-repo {:doc "Push this repo "
                            :la-test {:cmd ["bb" "push" "-m" "la" "-t" "la"]
                                      :skip? true}
                            :specific-cli-opts-kws [:commit :tag]
                            :pf :clj
                            :task-fn 'automaton-build-app.tasks.push-local-dir-to-repo/push-local-dir-to-repo}
   'report {:doc "Creates the reports of code analyzis"
            :la-test {:cmd ["bb" "report"]}
            :task-fn 'automaton-build-app.tasks.reports/reports}
   'update-deps {:doc "Update the dependencies, cider-nrepl and refactor are to be updated manually"
                 :la-test {:cmd ["bb" "updated-deps"]
                           :skip? true}
                 :pf :clj
                 :task-fn 'automaton-build-app.tasks.update-deps/update-deps}
   'wf-6 {:doc "Push the local version - create gha docker image - push to the repo"
          :group :wf
          :step 6
          :task-fn 'automaton-build-app.tasks.workflow.composer/composer
          :wk-tasks ['clean 'clean]}
   'ide {:doc "Quick tests to use during IDE usage"
         :group :ide
         :step 1
         :task-fn 'automaton-build-app.tasks.workflow.composer/composer
         :wk-tasks ['report 'format 'lint]}})
