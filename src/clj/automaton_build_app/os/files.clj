(ns automaton-build-app.os.files
  "Tools to manipulate local files
  Is a proxy to babashka.fs tools"
  (:require
   [automaton-build-app.log :as build-log]
   [babashka.fs :as fs]
   [clojure.string :as str]))

(def file-separator
  "Symbol to separate directories.
  Is usually `/` on linux based OS And `\\` on windows based ones"
  fs/file-separator)

(defn absolutize
  "Transform a file or dir name in an absolute path"
  [relative-path]
  (when relative-path
    (str (fs/absolutize relative-path))))

(defn delete-files
  "Deletes the files which are given in the list.
  They could be regular files or directory, when so the whole subtreee will be removed"
  [file-list]
  (doseq [file file-list]
    (if (fs/directory? file)
      (do
        (build-log/debug "Directory " (absolutize file) " is deleted")
        (fs/delete-tree file))
      (do
        (build-log/debug "File " (absolutize file) " is deleted")
        (fs/delete-if-exists file)))))

(defn- copy-files-or-dir-validate
  "Internal function to validate data for `copy-files-or-dir`"
  [files]
  (when-not (and (sequential? files)
                 (every? #(or (string? %)
                              (= java.net.URL
                                 (class %)))
                         files))
    (throw (ex-info "The `files` parameter should be a sequence of string or `java.net.URL`"
                    {:files files}))))

(defn copy-files-or-dir
  "Copy the files, even if they are directories to the target
  * `files` is a sequence of file or directory name, in absolute or relative form
  * `target-dir` is where files are copied to"
  [files target-dir]
  (copy-files-or-dir-validate files)
  (try
    (fs/create-dirs target-dir)
    (doseq [file files]
      (build-log/debug "Copy from " file " to " target-dir)
      (if (fs/directory? file)
        (do
          (build-log/trace (format "Copy directory `%s` to `%s`"
                                   (absolutize file)
                                   (absolutize target-dir)))
          (fs/copy-tree file target-dir
                        {:replace-existing true
                         :copy-attributes true}))
        (do
          (build-log/trace (format "Copy files `%s` to `%s`"
                                   (absolutize file)
                                   (absolutize target-dir)))
          (fs/copy file target-dir
                   {:replace-existing true
                    :copy-attributes true}))))
    (catch Exception e
      (throw (ex-info "Unexpected exception during copy"
                      {:exception e
                       :files files
                       :target-dir target-dir})))))

(defn directory-exists?
  "Check directory existance"
  [directory-path]
  (and (fs/exists? directory-path)
       (fs/directory? directory-path)))

(defn is-existing-file?
  "Check if this the path exist and is not a directory
  Params:
  * `filename` the file to check
  Returns `filename` if it is an existing file, nil otherwise"
  [filename]
  (when (and (fs/exists? filename)
             (not (fs/directory? filename)))
    filename))

(defn is-existing-dir?
  "Check if this the path exist and is a directory"
  [path]
  (and (fs/exists? path)
       (fs/directory? path)))

(defn create-dirs
  "Create a directory"
  [dir]
  (when (is-existing-file? dir)
    (throw (ex-info (format "Can't create a directory `%s` as a file already exists with that name"
                            (absolutize dir))
                    {:dir dir})))
  (when-not (fs/exists? dir)
    (try
      (fs/create-dirs dir)
      (catch Exception e
        (throw (ex-info (format "The parameter is not a valid directory: `%s`" dir)
                        {:dir (absolutize dir)
                         :exception e})))))
  true)

(defn remove-trailing-separator
  "If exists, remove the trailing separator in a path, remove unwanted spaces either"
  [path]
  (let [path (str/trim path)]
    (str/replace path (re-pattern (str file-separator "*$")) "")))

(defn create-file-path
  "Creates a path with the list of parameters.
  Removes the empty strings, add needed separators"
  [& dirs]
  (if (some? dirs)
    (->> dirs
         (map str)
         (filter #(not (str/blank? %)))
         (map remove-trailing-separator)
         (interpose file-separator)
         (apply str))
    "."))

(defn create-dir-path
  "Creates a path with the list of parameters.
  Removes the empty strings, add needed separators, including the trailing ones"
  [& dirs]
  (when-let [file-path (apply create-file-path dirs)]
    (str file-path file-separator)))

(defn search-files
  "Search files.
  * `root` is where the root directory of the search-files
  * `pattern` is a regular expression or a glob as described in [java doc](https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#getPathMatcher(java.lang.String))
  * `options` (Optional, default = {}) are boolean value for `:hidden`, `:recursive` and `:follow-lins`. See [babashka fs](https://github.com/babashka/fs/blob/master/API.md#glob) for details.
  For instance:
  * `(files/search-files \"\" \"**{.clj,.cljs,.cljc,.edn}\")` search all clj files in pwd directory"
  ([root pattern options]
   (when-not (directory-exists? root)
     (throw (ex-info (str root " is expected to be a directory") {:root root
                                                                  :pattern pattern
                                                                  :options options})))
   (into []
         (map str
              (fs/glob root pattern (merge {:hidden true
                                            :recursive true
                                            :follow-links true}
                                           options)))))
  ([root pattern]
   (search-files root pattern {})))

(defn match-extension?
  [filename & extensions]
  (some (fn [extension]
          (str/ends-with? filename extension))
        extensions))
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

(defn extract-path
  "Extract if the filename is a file, return the path that contains it,
  otherwise return the path itself"
  [filename]
  (when-not (str/blank? filename)
    (if (fs/directory? filename)
      filename
      (str
       (when (= (str file-separator)
                (str (first filename)))
         file-separator)
       (->> filename
            fs/components
            butlast
            (map str)
            (apply create-dir-path))))))

(defn read-file
  "Read the file `target-filename`"
  [target-filename]
  (build-log/trace (str "Reading file `" target-filename "`"))
  (try
    (slurp target-filename)
    (catch Exception e
      (build-log/error-exception e)
      nil)))

(defn spit-file
  "Spit the file, the directory where to store the file is created if necessary
  * `filename` is the name of the file to write, could be absolute or relative
  * `content` is the content to store there"
  [filename content]
  (let [filepath (extract-path filename)]
    (fs/create-dirs filepath)
    (spit filename content)))

(defn create-temp-dir
  "Creates a temorary directory managed by the system
  Params:
  * `sub-dirs` is an optional list of strings, each one is a sub directory
  Returns the string of the directory path"
  [& sub-dirs]
  (apply create-dir-path (->
                          (fs/create-temp-dir)
                          str)
         sub-dirs))

(defn filter-existing-dir
  "Filter only existing dirs
  Params:
  * `dirs` sequence of string of directories"
  [dirs]
  (apply vector
         (mapcat (fn [sub-dir]
                   (let [sub-dir-rpath (absolutize sub-dir)]
                     (when (directory-exists? sub-dir-rpath)
                       [sub-dir-rpath])))
                 dirs)))

(defn file-name
  "Return the file name without the path"
  [path]
  (fs/file-name path))

(defn write-file
  "Write `content` in the file `target-file`"
  [content target-filename]
  (build-log/trace (str "Writing file `" target-filename "`, content=" content))
  (try
    (spit target-filename content)
    (catch Exception e
      (throw (ex-info "Impossible to write the file"
                      {:target-filename target-filename
                       :exception e})))))

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
