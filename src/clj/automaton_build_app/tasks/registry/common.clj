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
   'blog {:doc "Generate the blog files"
          :la-test {:cmd ["bb" "blog"]}
          :pf :clj
          :task-fn 'automaton-build-app.tasks.blog/blog}
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
   'docstring {:doc "Generate the documentation based on docstring"
               :la-test {:cmd ["bb" "docstring"]}
               :pf :clj
               :task-fn 'automaton-build-app.tasks.docstring/docstring}
   'format-code {:doc "Format the whole documentation"
                 :la-test {:cmd ["bb" "format-code"]}
                 :task-fn 'automaton-build-app.tasks.format-code/format-code}
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
   '-error {:doc "On purpose run an error"
            :task-fn 'automaton-build-app.tasks.error/error
            :la-test {:cmd ["bb" "-error"]
                      :expected-exit-code 1}}
   '-is-cicd {:doc "Is runned on cicd"
              :la-test {:cmd ["bb" "-is-cicd" "-f"]}
              :specific-cli-opts-kws [:commit :force]
              :task-fn 'automaton-build-app.tasks.is-cicd/is-cicd}
   'la {:doc "Local acceptance test"
        :la-test {:cmd ["bb" "la"]
                  :skip? true}
        :task-fn 'automaton-build-app.tasks.la/la}
   'lint {:doc "linter"
          :la-test {:cmd ["bb" "lint"]}
          :task-fn 'automaton-build-app.tasks.lint/lint}
   'lbe-test {:doc "Local Backend test"
              :la-test {:cmd ["bb" "lbe-test"]}
              :task-fn 'automaton-build-app.tasks.lbe-test/lbe-test}
   'lfe-test {:doc "Local frontend test"
              :la-test {:cmd ["bb" "lfe-test"]}
              :task-fn 'automaton-build-app.tasks.lfe-test/lfe-test}
   'mermaid {:doc "Build all mermaid files"
             :la-test {:cmd ["bb" "mermaid"]}
             :task-fn 'automaton-build-app.tasks.mermaid/mermaid}
   'push-local-dir-to-repo {:doc "Push this repo "
                            :la-test {:cmd ["bb" "push-local-dir-to-repo" "-m" "la" "-t" "la"]
                                      :skip? true}
                            :specific-cli-opts-kws [:commit :tag]
                            :pf :clj
                            :task-fn 'automaton-build-app.tasks.push-local-dir-to-repo/push-local-dir-to-repo}
   'reports {:doc "Creates the reports of code analyzis"
             :la-test {:cmd ["bb" "reports"]}
             :task-fn 'automaton-build-app.tasks.reports/reports}
   'update-deps {:doc "Update the dependencies, cider-nrepl and refactor are to be updated manually"
                 :la-test {:cmd ["bb" "update-deps"]
                           :skip? true}
                 :pf :clj
                 :task-fn 'automaton-build-app.tasks.update-deps/update-deps}
   'vizualise-deps {:doc "Vizualise the dependencies in a graph"
                    :la-test {:cmd ["bb" "vizualise-deps"]}
                    :pf :clj
                    :task-fn 'automaton-build-app.tasks.vizualise-deps/vizualise-deps}
   'vizualise-ns {:doc "Vizualise the namespace in graph"
                  :la-test {:cmd ["bb" "vizualise-ns"]}
                  :pf :clj
                  :task-fn 'automaton-build-app.tasks.vizualise-ns/vizualise-ns}
   'wf-6 {:doc "Push the local version - create gha docker image - push to the repo"
          :group :wf
          :step 6
          :task-fn 'automaton-build-app.tasks.workflow.composer/composer
          :wk-tasks ['clean 'lint 'lbe-test 'lfe-test 'reports 'blog 'mermaid #_'vizualise-deps 'vizualise-ns 'format-code
                     'gha-container-publish 'push-local-dir-to-repo]}
   'gha {:doc "Github action tests - launched is automatically by github"
         :group :gha
         :step 1
         :wk-tasks ['-is-cicd 'lint 'lbe-test]
         :task-fn 'automaton-build-app.tasks.workflow.composer/composer}
   'wf-2 {:doc "Quick verifications and formatting for IDE usage"
          :group :wf
          :step 2
          :task-fn 'automaton-build-app.tasks.workflow.composer/composer
          :wk-tasks ['reports 'format-code 'lint]}})
