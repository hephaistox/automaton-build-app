(ns automaton-build-app.bb-tasks
  "Bb tasks"
  (:require [automaton-build-app.code-helpers.bb-edn :as bb-edn]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.schema :as build-schema]))

(def registry
  {'blog {:doc "Regenerate blog documents and pages",
          :task-fn 'automaton-build-app.tasks.doc-clj/cicd-doc,
          :pf :clj,
          :la-test {:cmd ["bb" "blog"]}},
   'clean {:doc "Clean cache files for compiles, and logs",
           :la-test {:cmd ["bb" "clean"]},
           :task-fn 'automaton-build-app.tasks.clean/clean},
   'clean-hard
     {:doc
        "Clean all files which are not under version control (it doesn't remove untracked file or staged files if there are eligible to `git add .`)",
      :la-test {:cmd ["bb" "clean-hard"], :process-opts {:in "q"}},
      :task-fn 'automaton-build-app.tasks.clean/clean-hard},
   'code-doc {:doc "Create the documentation of the app",
              :la-test {:cmd ["bb" "code-doc"]},
              :pf :clj,
              :task-fn 'automaton-build-app.tasks.doc-clj/code-doc},
   'compile-to-jar
     {:doc "Compile that code to a jar",
      :pf :clj,
      :la-test {:cmd ["bb" "compile-to-jar"]},
      :task-fn 'automaton-build-app.tasks.code-helpers-clj/compile-to-jar},
   'container-clear
     {:doc "Clear all local containers",
      :la-test {:cmd ["bb" "container-clear"], :skip? true},
      :task-fn
        'automaton-build-app.tasks.container-publication/container-clean},
   'container-list
     {:doc "List all available containers",
      :la-test {:cmd ["bb" "container-list"]},
      :task-fn 'automaton-build-app.tasks.container-publication/container-list},
   'format {:doc "Format the whole documentation",
            :la-test {:cmd ["bb" "format"]},
            :task-fn 'automaton-build-app.tasks.code-helpers/format-all},
   'gha {:doc "Github action command - is launched automatically by github",
         :la-test {:cmd ["bb" "gha" "-f"]},
         :pf :clj,
         :task-fn 'automaton-build-app.tasks.test-clj/gha},
   'gha-container-publish
     {:doc "Update the gha container to run that app",
      :la-test {:cmd ["bb" "gha-container-publish"], :skip? true},
      :task-fn
        'automaton-build-app.tasks.container-publication/push-gha-from-local},
   'gha-lconnect {:doc "Connect to a local container running this code",
                  :la-test {:cmd ["bb" "gha-lconnect"], :in "exit\n"},
                  :task-fn 'automaton-build-app.tasks.test/gha-lconnect},
   'la {:doc "Local acceptance test",
        :la-test {:cmd ["bb" "la"], :skip? true},
        :task-fn 'automaton-build-app.tasks.test/la},
   'lconnect {:doc "Local connect - repl",
              :la-test {:cmd ["bb" "lconnect"], :skip? true},
              :task-fn 'automaton-build-app.tasks.code-helpers/lconnect},
   'ltest {:doc "Local test",
           :la-test {:cmd ["bb" "ltest"]},
           :task-fn 'automaton-build-app.tasks.test/ltest},
   'publish {:doc "",
             :task-fn
               'automaton-build-app.tasks.publication-clj/push-from-local,
             :la-test {:cmd ["bb" "publish" "-t" "v-test"]}},
   'push {:doc "Push this repo",
          :la-test {:cmd ["bb" "push" "-m" "la" "-t" "la"], :skip? true},
          :pf :clj,
          :task-fn 'automaton-build-app.tasks.publication-clj/push-from-local},
   'report {:doc "Creates the reports of code analyzis",
            :la-test {:cmd ["bb" "report"]},
            :pf :clj,
            :task-fn 'automaton-build-app.tasks.code-analyze-clj/reports},
   'update-deps
     {:doc
        "Update the dependencies, cider-nrepl and refactor are to be updated manually",
      :la-test {:cmd ["bb" "updated-deps"], :skip? true},
      :pf :clj,
      :task-fn 'automaton-build-app.tasks.code-helpers-clj/update-deps}})

(def registry-schema
  [:map-of :symbol
   [:map {:closed true} [:doc :string] [:pf {:optional true} :keyword]
    [:task-fn :symbol]
    [:la-test
     [:map {:closed true} [:skip? {:optional true} :boolean]
      [:in {:optional true} :string] [:process-opts {:optional true} :map]
      [:cmd [:vector :string]]]]]])

(when (nil? (build-schema/valid? registry-schema registry))
  (build-log/error "The bb task registry does not comply the schema"))

(def all-tasks "Vector of task names (as strings)" (mapv str (keys registry)))

(defn update-bb-task
  "Create a bb task from a bb registry task
  Params:
  * `registry-bb-task`"
  [{:keys [doc pf task-fn], :as _registry-bb-task}]
  {:doc doc,
   :task (->> (cond-> ['execute-task (list 'quote task-fn)]
                pf (conj {:executing-pf pf}))
              (apply list))})

(defn add-bb-tasks
  "Add tasks from registry in the bb edn tasks"
  [registry-tasks bb-edn-tasks]
  (->> registry-tasks
       (map (fn [[k v]] [k (update-bb-task v)]))
       (into {})
       (merge bb-edn-tasks)))

(defn- remove-bb-tasks
  [to-be-removed in-bb-edn]
  (->> to-be-removed
       (into #{})
       (apply dissoc in-bb-edn)))

(defn update-bb-tasks
  "Update the bb tasks in the `bb.edn` file
  Params:
  * `dir` the directory where to look at the bb.edn file
  * `bb-tasks`
  * `exclude-tasks` "
  [dir bb-tasks exclude-tasks]
  (let [bb-tasks (map symbol bb-tasks)
        registry-bb-tasks (select-keys registry bb-tasks)]
    (bb-edn/update-bb-edn
      dir
      (fn [bb-content]
        (-> bb-content
            (update :tasks (partial add-bb-tasks registry-bb-tasks))
            (update :tasks (partial remove-bb-tasks exclude-tasks)))))))
