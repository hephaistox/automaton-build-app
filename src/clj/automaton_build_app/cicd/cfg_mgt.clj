(ns automaton-build-app.cicd.cfg-mgt
  "Adapter for configuration management

  Proxy to git"
  (:require [automaton-build-app.cicd.version :as build-version]
            [automaton-build-app.log :as build-log]
            [automaton-build-app.os.commands :as build-cmds]
            [automaton-build-app.os.files :as build-files]
            [clojure.string :as str]))

(defn git-installed?*
  "Returns true if git is properly installed
  Params:
  * `git-cmd` is an optional parameter to give an alternative git command"
  ([] (git-installed?* "git"))
  ([git-cmd]
   (try (or (zero? (ffirst (build-cmds/execute-with-exit-code [git-cmd "-v"
                                                               {:dir "."}])))
            (do (build-log/error "Git command does not work") false))
        (catch Exception e (build-log/error-exception e) false))))

(def git-installed?
  "Returns true if git is properly installed
  That version executes only once"
  (memoize git-installed?*))

(defn clean-hard
  "Configuration management comes back to the same state than the repository is freshly donwloaded
  Params:
  * `root-dir` is the repository where the cleansing is done
  * `interactive?` true by default, meaning the user is asked to confirm.
  Use `interactive?`=false with caution!!!!!"
  ([root-dir interactive?]
   (build-log/debug-format "Clean the repository `%s`" root-dir)
   (when (git-installed?)
     (build-cmds/execute-and-trace
       ["git" "clean" (str "-fqdx" (when interactive? "i")) {:dir root-dir}])))
  ([root-dir] (clean-hard root-dir true)))

(defn clone-repo-branch
  "Clone one branch of a remote repository to the `target-dir`
  Params:
  * `target-dir` is the directory where the repository should be cloned
  * `repo-address` the remote url where the repository is stored
  * `branch-name` is the name of the branch to download
  Return true if succesfull"
  [target-dir repo-address branch-name]
  (build-log/debug-format "Clone in repo `%s`, branch `%s` in `%s` "
                          repo-address
                          branch-name
                          target-dir)
  (when (git-installed?)
    (let [[exit-code message] (->> (build-cmds/execute-with-exit-code
                                     ["git" "clone" repo-address target-dir
                                      "--single-branch" "-b" branch-name
                                      "--depth" "1" {:dir target-dir}])
                                   first)]
      (cond (zero? exit-code) true
            (re-find #"Could not find remote branch" message)
              (do (build-log/error-format
                    "Branch `%s` does not exists in repo `%s`"
                    branch-name
                    repo-address)
                  false)
            (re-find #"Repository not found" message)
              (do (build-log/error-format "Repository `%s` not found"
                                          repo-address)
                  false)
            :else (do (build-log/error "Unexpected error during clone repo: "
                                       message)
                      false)))))

(defn create-and-switch-to-branch
  "In an existing repo stored in `dir`, creates a branch called `branch-name` and switch to it
  Params:
  * `dir` directory where the repo to update lies
  * `branch-name` the branch to create and switch to"
  [dir branch-name]
  (when (git-installed?)
    (let [branch-switch-res (build-cmds/execute-with-exit-code
                              ["git" "branch" branch-name {:dir dir}]
                              ["git" "switch" branch-name {:dir dir}])]
      (cond (every? #(= 0 (first %)) branch-switch-res) true
            :else (do (build-log/error-format
                        "Unexpected error during branch creation %s"
                        (map second branch-switch-res))
                      false)))))

(defn current-branch
  "Return the name of the current branch in `dir`
  Params:
  * `dir` directory where the repository to get the branch from"
  [dir]
  (when (git-installed?)
    (let [result (-> (build-cmds/execute-get-string
                       ["git" "branch" "--show-current" {:dir dir}])
                     first
                     str/split-lines
                     first)]
      (build-log/debug-format
        "Retrieve the current branch in directory `%s`, found = `%s`"
        dir
        result)
      result)))

(defn commit-and-push
  "Push to its `origin` what is the working state in `dir` to branch `branch-name`
 Params:
  * `dir` directory where the repository is stored
  * `msg` message for the commit
  * `branch-name` branch name"
  [dir msg branch-name]
  (let [msg (or msg "commit")]
    (when (git-installed?)
      (let [commit-res (build-cmds/execute-with-exit-code
                         ["git" "add" "." {:dir dir}]
                         ["git" "commit" "-m" msg {:dir dir}]
                         ["git" "push" "--set-upstream" "origin" branch-name
                          {:dir dir}])]
        (case (first (build-cmds/first-cmd-failing commit-res))
          nil (do (build-log/info "Successfully pushed") true)
          1 (do (build-log/debug "Nothing to commit, skip the push") false)
          2 (do (build-log/debug "Push has failed") false)
          :else (do (build-log/error
                      "Unexpected error during commit-and-push : "
                      (into [] commit-res))
                    false))))))

(defn commit-and-push-and-tag
  "Push to its `origin` what is the working state in `dir` to branch `branch-name`
 Params:
  * `dir` directory where the repository is stored
  * `msg` message for the commit
  * `branch-name` branch name
  * `version` version to use in the tag
  * `tag-msg` is the message of the tag"
  [dir msg branch-name version tag-msg]
  (build-log/info-format "Commit and push in progress")
  (let [msg (or msg "commit")]
    (when (git-installed?)
      (let [commit-res (build-cmds/execute-with-exit-code
                         ["git" "add" "." {:dir dir}]
                         ["git" "commit" "-m" msg {:dir dir}]
                         ["git" "tag" "-f" "-a" version "-m" tag-msg {:dir dir}]
                         ["git" "push" "--tags" "--set-upstream" "origin"
                          branch-name {:dir dir}])
            [cmd-failing message] (build-cmds/first-cmd-failing commit-res)]
        (case cmd-failing
          nil (do (build-log/info "Successfully pushed") true)
          1 (do (build-log/info-format "Nothing to commit, skip the push")
                false)
          2 (do (build-log/error-format "Tag has failed - %s" message) false)
          3 (do (build-log/error-format "Push has failed - %s" message) false)
          :else (do (build-log/error
                      "Unexpected error during commit-and-push : "
                      (into [] commit-res))
                    false))))))

(defn- prepare-cloned-repo-on-branch
  "Clone the repo in diectory `tmp-dir`, the repo at `repo-address` is copied on branch `branch-name`, if it does not exist create a branch based on `base-branch-name`
  Params:
  * `tmp-dir`
  * `repo-address`
  * `base-branch-name`
  * `branch-name`"
  [tmp-dir repo-address base-branch-name branch-name]
  (if (clone-repo-branch tmp-dir repo-address branch-name)
    (do (build-log/debug-format "Successfully cloned branch %s" branch-name)
        true)
    (do
      (build-log/debug-format
        "Branch `%s` does not exist on the remote repo, it will be created locally"
        branch-name)
      (when (clone-repo-branch tmp-dir repo-address base-branch-name)
        (create-and-switch-to-branch tmp-dir branch-name)))))

(defn- squash-local-files-and-push
  "Considering a cloned repo in `tmp-dir`, replace the current files with the ones in `source-dir`
  Remind that monorepo is our the source of truth, so use it with caution
  Params:
  * `tmp-dir` where the cloned repo is stored, the branch should already be sync with remote repo and currently selected
  * `source-dir` the files that will be copied from
  * `commit-message` the message for the commit,
  * `tag-msg` the message of the tag
  * `major-version`"
  [tmp-dir source-dir commit-message tag-msg major-version]
  (when (git-installed?)
    (->> (build-files/search-files tmp-dir "*")
         (filter (fn [file] (not (str/ends-with? file ".git"))))
         build-files/delete-files)
    (build-files/copy-files-or-dir [source-dir] tmp-dir)
    (commit-and-push-and-tag tmp-dir
                             commit-message
                             (current-branch tmp-dir)
                             (build-version/version-to-push major-version)
                             tag-msg)))

(defn- validate-branch-name
  "Validate the name of the branch
  `main` and `master` branches are forbid except if `force?` is ok
  * `force?` boolean to force
  * `validate-branch-name
  Returns true if the name is validated"
  [force? branch-name]
  (if (and (not force?) (contains? #{"main" "master"} branch-name))
    (do
      (build-log/error
        "Push to main or master is refused. If you want to confirm, use -f option")
      false)
    true))

(defn push-local-dir-to-repo
  "Use that function to push the files in the `source-dir` to the repo
  Params:
  * `source-dir` local directory where the sources are stored, before being pushed to the remote repo
  * `repo-address` the address of the repo where to push
  * `base-branch-name` if branch-name does not exist, it will be created based on `base-branch-name`
  * `force?` (optional default false) if false, will refuse to push on master or main branches"
  ([source-dir repo-address base-branch-name commit-msg tag-msg major-version
    force?]
   (when (git-installed?)
     (build-log/info "Pushing from local directory to repository")
     (let [branch-name (current-branch ".")]
       (build-log/trace-map "Push local directories"
                            :source-dir source-dir
                            :repo-address repo-address
                            :base-branch-name base-branch-name
                            :branch-name branch-name
                            :commit-msg commit-msg
                            :tag-msg tag-msg
                            :force? force?)
       (when (validate-branch-name force? branch-name)
         (let [tmp-dir (build-files/create-temp-dir)]
           (when (prepare-cloned-repo-on-branch tmp-dir
                                                repo-address
                                                base-branch-name
                                                branch-name)
             (build-log/debug
               "Pushing from local directory to repository - repo is ready")
             (squash-local-files-and-push tmp-dir
                                          source-dir
                                          commit-msg
                                          tag-msg
                                          major-version)))))))
  ([source-dir repo-address base-branch-name commit-msg tag-msg major-version]
   (push-local-dir-to-repo source-dir
                           repo-address
                           base-branch-name
                           commit-msg
                           tag-msg
                           major-version
                           false)))

(defn extract-app-from-repo
  "Use that function to push the last commit of monorepo for branch `branch-name`
  Params:
  * `monorepo-address` adress of the monorepo
  * `app-repo-address` the address of the repo where to push
  * `branch-name` the branch where to push
  * `sub-dir` directory in monorepo cloned repo which will be copied to app-repo
  * `commit-msg` commit message
  * `tag-msg` tag message
  * `force?` (optional default false) if false, will refuse to push on master or main branches"
  ([monorepo-address app-repo-address branch-name sub-dir commit-msg tag-msg
    major-version]
   (extract-app-from-repo monorepo-address
                          app-repo-address
                          branch-name
                          sub-dir
                          commit-msg
                          tag-msg
                          major-version
                          false))
  ([monorepo-address app-repo-address branch-name sub-dir commit-msg tag-msg
    major-version force?]
   (build-log/debug-format "Extract the app from repo on branch `%s`"
                           branch-name)
   (when (git-installed?)
     (when (validate-branch-name force? branch-name)
       (let [monorepo-tmp-dir (build-files/create-temp-dir)]
         (build-log/trace-format "Clone monorepo in directory `%s`"
                                 monorepo-tmp-dir)
         (when (clone-repo-branch monorepo-tmp-dir monorepo-address branch-name)
           (let [app-tmp-dir (build-files/create-temp-dir)]
             (when (clone-repo-branch app-tmp-dir app-repo-address branch-name)
               (squash-local-files-and-push
                 app-tmp-dir
                 (build-files/create-dir-path monorepo-tmp-dir sub-dir)
                 commit-msg
                 tag-msg
                 major-version)))))))))
