(ns automaton-build-app.tasks.doc
  "All the functions related to blogging/creating content"
  (:require
   [automaton-build-app.os.commands :as build-cmds]))

(defn blog-task
  "Blog task"
  []
  (build-cmds/execute ["clj" "-X" "automaton-build-app.doc.blog/blog-process"]))
