(ns automaton-build-app.code-helpers.frontend-compiler
  "Gather front end compilers toolings. Currently use shadow on npx "
  (:require
   [automaton-build-app.os.commands :as build-cmds]
   [automaton-build-app.log :as build-log]
   [automaton-build-app.os.edn-utils :as build-edn-utils]
   [automaton-build-app.os.files :as build-files]))

(def shadow-cljs-edn
  "shadow-cljs.edn")

(defn npx-installed?*
  "Check if npx is installed
  Params:
  * `dir` where npx should be executed
  * `npx-cmd` (Optional, default=npx) parameter to tell the npx command"
  ([dir]
   (npx-installed?* dir "npx"))
  ([dir npx-cmd]
   (every? string?
           (build-cmds/execute-get-string [npx-cmd "-v" {:dir dir}]))))

(def npx-installed?
  (memoize npx-installed?*))

(defn shadow-installed?*
  "Check if shadow-cljs is installed
  Params:
  * `dir` where to check if `shadow-cljs` is installed
  * `shadow-cmd` (Optional, default=shadow-cljs) parameter to tell the shadow cljs command
  * `npx-cmd` (Optional, default=npx) parameter to tell the npx command"
  ([dir]
   (shadow-installed?* dir "shadow-cljs" "npx"))
  ([dir shadow-cmd npx-cmd]
   (when (npx-installed? npx-cmd)
     (every? string?
             (build-cmds/execute-get-string [npx-cmd shadow-cmd "-info"
                                             {:dir dir}])))))

(def shadow-installed?
  (memoize shadow-installed?*))

(defn compile-target
  "Compile the target given as a parameter, in dev mode
  Params:
  * `npx-cmd` optional parameter to tell the npx command
  * `target-alias` the name of the alias in `shadow-cljs.edn` to compile
  * `dir` the frontend root directory"
  [npx-cmd target-alias dir]
  (when (shadow-installed? dir)
    (build-log/trace (build-cmds/execute ["npm" "install" {:dir dir}]
                                         [npx-cmd "shadow-cljs" "compile" target-alias
                                          {:dir dir}]))))

(defn test-fe
  "Test the target frontend
  Params:
  * `dir` the frontend root directory"
  [dir]
  (shadow-installed? dir)
  (build-cmds/execute-and-trace ["npm" "install" {:dir dir}]
                                ["npx" "shadow-cljs" "compile" "karma-test"
                                 {:dir dir}]
                                ["npx" "karma" "start" "--single-run"
                                 {:dir dir}]))

(defn create-size-optimization-report
  "Create a report on size-optimization
  Params:
  * `dir` the frontend root directory
  * `target-file` target file"
  [dir target-file]
  (build-log/debug "Generate the size optimization report in " target-file)
    ;; The output of the command is useless, as it is pushed in the targetfile
  (build-cmds/execute-and-trace ["npx" "shadow-cljs" "run" "shadow.cljs.build-report" "app" target-file
                                 {:dir dir}]))

(defn watch-modifications
  "Watch modification on code on cljs part, from tests or app
   Params:
   * `dir` the frontend root directory"
  [dir main-css custom-css compiled-styles-css]
  ;(build-files/create-file-path "resources" "css" "custom.css")
  ;(build-files/create-file-path "resources" "css" "main.css")
  ;;(build-files/create-file-path "resources" "public" "css" "compiled" "styles.css")
  (build-cmds/execute-and-trace ["npm" "install" {:dir dir}]
                                ["npx" "tailwindcss" "-i" main-css custom-css "-o" compiled-styles-css "--watch" {:dir dir}]
                                ["npx" "shadow-cljs" "watch" "app" "karma-test" "browser-test" {:dir dir}]))

(defn load-shadow-cljs
  "Read the shadow-cljs of an app
  Params:
  * `dir` the directory where to
  Returns the content as data structure"
  [dir]
  (let [shadow-filepath (build-files/create-file-path dir
                                        shadow-cljs-edn)]
    (when (build-files/is-existing-file? shadow-filepath)
      (build-edn-utils/read-edn shadow-filepath))))

(defn extract-paths
  "Extract paths from the shadow cljs file content
  Params:
  * `shadow-cljs-content` is the content of a shadow-cljs file
  Return a flat vector of all source paths"
  [shadow-cljs-content]
  (:source-paths shadow-cljs-content))
