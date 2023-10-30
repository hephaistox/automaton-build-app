(ns automaton-build-app.os.files
  "Tools to manipulate local files

  By convention, a directory always ends up with `/`

  Is a proxy to babashka.fs tools"
  (:require [automaton-build-app.log :as build-log]
            [babashka.fs :as fs]
            [clojure.string :as str]))

;; ***********************
;; Manipulate file path (need no access)
;; ***********************
(def file-separator
  "Symbol to separate directories.
  Is usually `/` on linux based OS And `\\` on windows based ones"
  fs/file-separator)

(defn change-extension
  "Change the extension"
  [file-name new-extension]
  (str (fs/strip-ext file-name) new-extension))

(defn remove-trailing-separator
  "If exists, remove the trailing separator in a path, remove unwanted spaces either"
  [path]
  (let [path (str/trim path)]
    (if (= (str file-separator) (str (last path)))
      (->> (dec (count path))
           (subs path 0)
           remove-trailing-separator)
      path)))

(defn remove-useless-path-elements
  "When generated, it may happen that paths are using useless elements, for instance:
  * `//` double separator
  * `/./` current directory used a subdir"
  [dir]
  (loop [dir dir]
    (when dir
      (let [old-dir dir
            dir (-> dir
                    (str/replace (str file-separator file-separator)
                                 (str file-separator))
                    (str/replace (str file-separator "." file-separator)
                                 file-separator))]
        (if (= old-dir dir) dir (recur dir))))))

(defn create-file-path
  "Creates a path with the list of parameters.
  Removes the empty strings, add needed separators"
  [& dirs]
  (if (empty? dirs)
    (build-log/warn "Invalid create file path with no file set")
    (-> (if (some? dirs)
            (->> dirs
                 (mapv str)
                 (filter #(not (str/blank? %)))
                 (mapv remove-trailing-separator)
                 (interpose file-separator)
                 (apply str))
            "./")
        remove-useless-path-elements
        str)))

(defn create-dir-path
  "Creates a path with the list of parameters.
  Removes the empty strings, add needed separators, including the trailing ones"
  [& dirs]
  (let [dir (apply create-file-path dirs)]
    (if (str/blank? dir) "" (str dir file-separator))))

(defn create-absolute-dir-path
  "Creates an absolute path with the list of parameters.
  Removes the empty strings, add needed separators, including the trailing ones"
  [& dirs]
  (str (apply create-file-path file-separator dirs) file-separator))

(defn match-extension?
  "Returns true if the filename match the at least one of the extensions
  Params:
  * `filename`
  * `extensions` list of extension represented as a string to be tested"
  [filename & extensions]
  (when-not (str/blank? filename)
    (some (fn [extension] (str/ends-with? filename extension)) extensions)))

(defn file-in-same-dir
  "Use the relative-name to create in file in the same directory than source-file"
  [source-file relative-name]
  (let [source-subdirs (fs/components source-file)
        subdirs (mapv str
                  (if (fs/directory? source-file)
                    source-subdirs
                    (butlast source-subdirs)))
        new-name (conj subdirs relative-name)]
    (apply create-file-path new-name)))

(defn is-absolute?
  "Returns true if the path is absolute
  Params:
  * `filename`"
  [filename]
  (= (str file-separator) (str (first filename))))

(defn extract-path
  "Extract if the filename is a file, return the path that contains it,
  otherwise return the path itself
  Params:
  * `filename`"
  [filename]
  (when-not (str/blank? filename)
    (if (fs/directory? filename)
      filename
      (let [filepath (->> filename
                          fs/components
                          butlast
                          (mapv str))]
        (cond (= [] filepath) ""
              (is-absolute? filename)
                (apply create-dir-path file-separator filepath)
              :else (apply create-dir-path filepath))))))

;; ***********************
;; Change files on disk
;; ***********************

(defn absolutize
  "Transform a file or dir name in an absolute path"
  [relative-path]
  (when relative-path (str (fs/absolutize relative-path))))

(defn- copy-files-or-dir-validate
  "Internal function to validate data for `copy-files-or-dir`"
  [files]
  (if (and (sequential? files)
           (every? #(or (string? %) (= java.net.URL (class %))) files))
    true
    (do
      (build-log/error-exception
        (ex-info
          "The `files` parameter should be a sequence of string or `java.net.URL`"
          {:files files}))
      false)))

(defn copy-files-or-dir
  "Copy the files, even if they are directories to the target
  * `files` is a sequence of file or directory name, in absolute or relative form
  * `target-dir` is where files are copied to"
  [files target-dir]
  (when (copy-files-or-dir-validate files)
    (try (fs/create-dirs target-dir)
         (doseq [file files]
           (build-log/debug "Copy from " file " to " target-dir)
           (if (fs/directory? file)
             (do (build-log/trace-format "Copy directory `%s` to `%s`"
                                         (absolutize file)
                                         (absolutize target-dir))
                 (fs/copy-tree file
                               target-dir
                               {:replace-existing true, :copy-attributes true}))
             (do (build-log/trace-format "Copy files `%s` to `%s`"
                                         (absolutize file)
                                         (absolutize target-dir))
                 (fs/copy file
                          target-dir
                          {:replace-existing true, :copy-attributes true}))))
         true
         (catch Exception e
           (build-log/error-exception
             (ex-info "Unexpected exception during copy"
                      {:exception e, :files files, :target-dir target-dir}))
           false))))

(defn delete-files
  "Deletes the files which are given in the list.
  They could be regular files or directory, when so the whole subtreee will be removed"
  [file-list]
  (doseq [file file-list]
    (when (fs/exists? file)
      (if (fs/directory? file)
        (do (build-log/debug "Directory " (absolutize file) " is deleted")
            (fs/delete-tree file))
        (do (build-log/debug "File " (absolutize file) " is deleted")
            (fs/delete-if-exists file))))))

(defn modified-since
  "Return true if anchor is older than one of the file in file-set"
  [anchor file-set]
  (let [file-set (filter some? file-set)]
    (when anchor (seq (fs/modified-since anchor file-set)))))

(defn current-dir
  "Return current dir"
  []
  (-> (fs/cwd)
      (str file-separator)))

(defn relativize-to-pwd
  "Remove the current pwd to the filename"
  [filename]
  (when filename
    (let [res (str/replace filename
                           (-> (current-dir)
                               re-pattern)
                           "")]
      (if (str/blank? res) "." res))))

(defn directory-exists?
  "Check directory existance"
  [directory-path]
  (and (fs/exists? directory-path) (fs/directory? directory-path)))

(defn is-existing-file?
  "Check if this the path exist and is not a directory
  Params:
  * `filename` the file to check
  Returns `filename` if it is an existing file, nil otherwise"
  [filename]
  (when (and (fs/exists? filename) (not (fs/directory? filename))) filename))

(defn filter-to-existing-files
  "Check if this the path exist and is not a directory
  Params:
  * `filename` the file to check
  Returns `filename` if it is an existing file, nil otherwise"
  [& filenames]
  (-> (filter (fn [filename]
                (if (is-existing-file? filename)
                  filename
                  (do (build-log/warn-format "File `%s` is filtered" filename)
                      false)))
              filenames)
      vec))

(defn is-existing-dir?
  "Check if this the path exist and is a directory"
  [path]
  (and (fs/exists? path) (fs/directory? path)))

(defn create-dirs
  "Create a directory
  Returns the directory if ok
  Params:
  * `dir` directory to create"
  [dir]
  (if (is-existing-file? dir)
    (build-log/warn-format
      "Can't create a directory `%s` as a file already exists with that name"
      (absolutize dir))
    (if (fs/exists? dir)
      (build-log/debug-format
        "Directory creation is skipped as the directory `%s` already exists"
        dir)
      (try (fs/create-dirs dir)
           dir
           (catch Exception e
             (build-log/warn-exception
               (ex-info (format "Directory creation has failed `%s`"
                                (absolutize dir))
                        {:dir dir}
                        e)))))))

(defn search-files
  "Search files.
  * `root` is where the root directory of the search-files
  * `pattern` is a regular expression or a glob as described in [java doc](https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#getPathMatcher(java.lang.String))
  * `options` (Optional, default = {}) are boolean value for `:hidden`, `:recursive` and `:follow-lins`. See [babashka fs](https://github.com/babashka/fs/blob/master/API.md#glob) for details.
  For instance:
  * `(files/search-files \"\" \"**{.clj,.cljs,.cljc,.edn}\")` search all clj files in pwd directory"
  ([root pattern options]
   (if (directory-exists? root)
     (mapv str
       (fs/glob root
                pattern
                (merge {:hidden true, :recursive true, :follow-links true}
                       options)))
     (do (build-log/warn-format "Search aborted as `%s` is not a directory"
                                root)
         (build-log/trace-map "search parameters"
                              :root root
                              :pattern pattern
                              :options options)
         [])))
  ([root pattern] (search-files root pattern {})))

(defn read-file
  "Read the file `target-filename`"
  [target-filename]
  (build-log/trace-format "Reading file `%s`" target-filename)
  (try (slurp target-filename)
       (catch Exception e (build-log/error-exception e) nil)))

(defn create-parent-dirs
  "Ensures the parents directory exist, so file saving will be sucessful
  Params:
  * `filename`"
  [filename]
  (let [filepath (extract-path filename)]
    (try (when-not (str/blank? filepath) (fs/create-dirs filepath))
         (catch Exception e
           (build-log/error-exception (ex-info "Impossible to create directory"
                                               {:caused-by e,
                                                :filename filename,
                                                :filepath filepath}))))))

(defn spit-file
  "Spit the file, the directory where to store the file is created if necessary
  * `filename` is the name of the file to write, could be absolute or relative
  * `content` is the content to store there"
  [filename content]
  (build-log/trace-format "Writing file `%s`" filename)
  (try (create-parent-dirs filename)
       (spit filename content)
       (catch Exception e
         (throw (ex-info "Impossible to write the file"
                         {:target-filename filename, :exception e})))))

(defn create-temp-dir
  "Creates a temorary directory managed by the system
  Params:
  * `sub-dirs` is an optional list of strings, each one is a sub directory
  Returns the string of the directory path"
  [& sub-dirs]
  (let [tmp-dir (apply create-dir-path
                  (-> (fs/create-temp-dir)
                      str)
                  sub-dirs)]
    (create-dirs tmp-dir)
    tmp-dir))

(defn filter-existing-dir
  "Filter only existing dirs
  Params:
  * `dirs` sequence of string of directories"
  [dirs]
  (apply vector
    (mapcat (fn [sub-dir]
              (let [sub-dir-rpath (absolutize sub-dir)]
                (when (directory-exists? sub-dir-rpath) [sub-dir-rpath])))
      dirs)))

(defn filename
  "Return the file name without the path"
  [path]
  (fs/file-name path))

(defn sorted-absolutize-dirs
  "Create a proper list of absolutized deduped directories, filtered
  Params:
  * `base-dir` is where the relative dirs will start
  * `relative-dirs` are the relative directories that needed to be absolutized"
  [base-dir & relative-dirs]
  (->> relative-dirs
       (mapv (comp absolutize (partial create-dir-path base-dir)))
       filter-existing-dir
       dedupe
       sort
       (into [])))

(defn for-each
  "Apply fn-each on each files in a directory"
  [dir fn-each]
  (when (is-existing-dir? dir)
    (doseq [file (fs/list-dir dir)] (fn-each (str file)))))

(defn create-temp-file
  "Create a temporary file
  Params:
  * `filename` name of the file (optional)"
  [& filename]
  (-> (fs/create-temp-file {:suffix (apply str filename)})
      str))

(defn search-in-parents
  "Search in parents directories
  Params:
  * `dir` starting point for the search
  * `file-or-dir` file or directory to search for"
  [dir file-or-dir]
  (loop [dir (absolutize dir)]
    (let [file-candidate (create-file-path (str dir) file-or-dir)]
      (if (fs/exists? file-candidate)
        dir
        (when-not (str/blank? dir) (recur (str (fs/parent dir))))))))

(defn make-executable
  "Make a file executable"
  [filename]
  (when (is-existing-file? filename)
    (fs/set-posix-file-permissions filename (fs/str->posix "rwx------"))))
