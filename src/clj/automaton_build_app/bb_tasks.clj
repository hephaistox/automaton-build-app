(ns automaton-build-app.bb-tasks
  "Bb tasks"
  (:require [automaton-build-app.code-helpers.bb-edn :as bb-edn]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.schema :as build-schema]))

(def registry
  {'clean {:doc "Clean cache files for compiles, and logs",
           :la-test {:cmd ["bb" "clean"]},
           :task-fn 'automaton-build-app.tasks.clean/clean},
   'clean-hard
     {:doc
        "Clean all files which are not under version control (it doesn't remove untracked file or staged files if there are eligible to `git add .`)",
      :la-test {:cmd ["bb" "clean-hard"], :process-opts {:in "q"}},
      :task-fn 'automaton-build-app.tasks.clean-hard/clean-hard},
   'code-doc {:doc "Create the documentation of the app",
              :la-test {:cmd ["bb" "code-doc"]},
              :pf :clj,
              :task-fn 'automaton-build-app.tasks.code-doc/code-doc},
   'compile-to-jar {:doc "Compile that code to a jar",
                    :pf :clj,
                    :la-test {:cmd ["bb" "compile-to-jar"]},
                    :task-fn
                      'automaton-build-app.tasks.compile-to-jar/compile-to-jar},
   'container-clear
     {:doc "Clear all local containers",
      :la-test {:cmd ["bb" "container-clear"], :skip? true},
      :task-fn 'automaton-build-app.tasks.container-clear/container-clear},
   'container-list {:doc "List all available containers",
                    :la-test {:cmd ["bb" "container-list"]},
                    :task-fn
                      'automaton-build-app.tasks.container-list/container-list},
   'format {:doc "Format the whole documentation",
            :la-test {:cmd ["bb" "format"]},
            :task-fn 'automaton-build-app.tasks.format-files/format-files},
   'gha {:doc "Github action command - is launched automatically by github",
         :la-test {:cmd ["bb" "gha" "-f"], :skip? true},
         :pf :clj,
         :task-fn 'automaton-build-app.tasks.gha/gha},
   'gha-container-publish
     {:doc "Update the gha container to run that app",
      :la-test {:cmd ["bb" "gha-container-publish"], :skip? true},
      :task-fn
        'automaton-build-app.tasks.gha-container-publish/gha-container-publish},
   'gha-lconnect
     {:doc "Connect to a local container running this code",
      :la-test
        {:cmd ["bb" "gha-lconnect"], :skip? true, :process-opts {:in "exit\n"}},
      :task-fn 'automaton-build-app.tasks.gha-lconnect/gha-lconnect},
   'ide {:doc "Quick tests to use during IDE",
         :la-test {:cmd ["bb" "ide"]},
         :task-fn 'automaton-build-app.tasks.ide/ide},
   'la {:doc "Local acceptance test",
        :la-test {:cmd ["bb" "la"], :skip? true},
        :task-fn 'automaton-build-app.tasks.la/la},
   'lconnect {:doc "Local connect - repl",
              :la-test {:cmd ["bb" "lconnect"], :skip? true},
              :task-fn 'automaton-build-app.tasks.lconnect/lconnect},
   'lint {:doc "linter",
          :la-test {:cmd ["bb" "lint"]},
          :task-fn 'automaton-build-app.tasks.lint/lint},
   'ltest {:doc "Local test",
           :la-test {:cmd ["bb" "ltest"]},
           :task-fn 'automaton-build-app.tasks.ltest/ltest},
   'push {:doc "Push this repo",
          :la-test {:cmd ["bb" "push" "-m" "la" "-t" "la"], :skip? true},
          :pf :clj,
          :task-fn 'automaton-build-app.tasks.push/push},
   'report {:doc "Creates the reports of code analyzis",
            :la-test {:cmd ["bb" "report"]},
            :task-fn 'automaton-build-app.tasks.reports/reports},
   'update-deps
     {:doc
        "Update the dependencies, cider-nrepl and refactor are to be updated manually",
      :la-test {:cmd ["bb" "updated-deps"], :skip? true},
      :pf :clj,
      :task-fn 'automaton-build-app.tasks.update-deps/update-deps}})

(def registry-schema
  [:map-of :symbol
   [:map {:closed true} [:doc :string] [:pf {:optional true} :keyword]
    [:task-fn :symbol]
    [:la-test
     [:map {:closed true} [:skip? {:optional true} :boolean]
      [:process-opts {:optional true} :map] [:cmd [:vector :string]]]]]])

(when (nil? (build-schema/valid? registry-schema registry))
  (build-log/error "The bb task registry does not comply the schema"))

(def all-tasks "Vector of task names (as strings)" (mapv str (keys registry)))

(defn update-bb-task
  "Create a bb task from a bb registry task
  Params:
  * `registry-bb-task`"
  [{:keys [doc pf task-fn], :as _registry-bb-task}]
  (let [prepared-list (cond-> ['execute-task (list 'quote task-fn)]
                        pf (conj {:executing-pf pf}))]
    {:doc doc,
     :task (->> prepared-list
                (apply list))}))

(defn add-bb-tasks
  "Add tasks from registry in the bb edn tasks"
  [registry-tasks bb-edn-tasks]
  (->> registry-tasks
       (map (fn [[k v]] [k (update-bb-task v)]))
       (into {})
       (merge bb-edn-tasks)))

(defn remove-bb-tasks
  [registry-tasks bb-edn-tasks]
  (let [bb-edn-tasks (into {} bb-edn-tasks)]
    (->> registry-tasks
         (map symbol)
         (into #{})
         (apply dissoc bb-edn-tasks))))

(defn update-bb-tasks*
  "Update the bb tasks in the `bb.edn` file
  Params:
  * `registry-bb-tasks`
  * `exclude-tasks`
  * `bb-content`
  "
  [registry-bb-tasks exclude-tasks bb-content]
  (-> bb-content
      (update :tasks (partial add-bb-tasks registry-bb-tasks))
      (update :tasks (partial remove-bb-tasks exclude-tasks))))

(defn update-bb-tasks
  "Update the bb tasks in the `bb.edn` file
  Params:
  * `dir` the directory where to look at the bb.edn file
  * `bb-tasks` list of tasks to be selected (could be string but will be changed into symbols)
  * `exclude-tasks` set of tasks to exclude"
  [dir bb-tasks exclude-tasks]
  (let [bb-tasks (map symbol bb-tasks)
        registry-bb-tasks (select-keys registry bb-tasks)]
    (when (bb-edn/update-bb-edn
            dir
            (partial update-bb-tasks* registry-bb-tasks exclude-tasks))
      (build-log/info "The `bb.edn` file has changed")
      true)))
