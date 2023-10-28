(ns automaton-build-app.code-helpers.analyze.css
  (:require [automaton-build-app.code-helpers.analyze.utils :as
             build-analyze-utils]
            [automaton-build-app.file-repo.raw :as build-filerepo-raw]
            [automaton-build-app.file-repo.text :as build-filerepo-text]
            [automaton-build-app.utils.namespace :as build-namespace]))

(def css-pattern
  #":class\s*\"|:(a|abbr|acronym|address|applet|area|article|aside|audio|b|automaton|basefont|bdi|bdo|big|blockquote|body|br|button|canvas|caption|center|cite|code|col|colgroup|data|datalist|dd|del|details|dfn|dialog|dir|div|dl|dt|em|embed|fieldset|figcaption|figure|font|footer|form|frame|frameset|h1|h2|h3|h4|h5|head|header|hr|html|i|iframe|img|input|ins|kbd|label|legend|li|link|main|map|mark|meta|meter|nav|noframes|noscript|object|ol|optgroup|option|output|p|param|picture|pre|progress|q|rp|rt|ruby|s|samp|script|section|select|small|source|span|strike|strong|style|sub|summary|sup|svg|table|tbody|td|template|textarea|tfoot|th|thead|time|title|tr|track|tt|u|ul|var|video|wbr)(?:\#|\.)")

(defn css-matches
  "List code lines matching comments
  Params:
  * `clj-repo`"
  [clj-repo]
  (let [regexp-filerepo-matcher
          (->> ['automaton-build-app.code-helpers.analyze.css-test]
               (map build-namespace/ns-to-file)
               (into #{}))
        matches (-> clj-repo
                    (build-filerepo-raw/exclude-files regexp-filerepo-matcher)
                    (build-filerepo-text/filecontent-to-match css-pattern))]
    (->> matches
         (map (fn [[filename [_whole-match comment]]] [comment filename]))
         vec)))

(defn save-report
  [matches filename]
  (build-analyze-utils/save-report matches
                                   "List of forbidden css forms"
                                   filename
                                   (fn [[match filename]]
                                     (format "%s -> [%s]" match filename))))

(defn assert-empty
  [matches]
  (build-analyze-utils/assert-empty matches "Found forbidden css code"))
