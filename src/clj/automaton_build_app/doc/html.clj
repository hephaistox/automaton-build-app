(ns automaton-build-app.doc.html
  "Manipulate html"
  (:require
   [automaton-build-app.doc.markdown :as build-markdown]
   [automaton-build-app.os.files :as build-files]
   [hiccup2.core :as hiccup]))

(defn- hephaistox-logo
  "Hephaistox logo clj-pdf element created for header"
  [src header-id]
  [:img
   {:id header-id
    :src src}])

(defn header-str
  [{:keys [logo-path
           header-id]}]
  (str (hiccup/html (hephaistox-logo logo-path
                                     header-id))))

(defn md->html-str
  "Transform a md file to an HTML, provided as a string"
  [{:keys [md-path html-path header-id]}]
  (let [_create-html-file (build-markdown/md-to-html md-path
                                                     html-path)
        html-file-str (build-files/read-file html-path)
        header (when header-id
                 (header-str {:logo-path "logo.jpg"
                              :header-id header-id}))]
    (str header
         html-file-str)))
