(ns automaton-build-app.code-helpers.analyze.reports
  "List the reports to generate during the analyzis"
  (:require
   [automaton-build-app.file-repo.text-analyzis.regexp :as build-repo-text-analyzis-regexp]))

(comment
  (def reports
    "Mandatory data are:
  * `pattern` what to search in the code
  * `pattern-match-to-report` build a report entry based on a pattern match
  Optionals: if that data are null the associated feature is skipped
  * `ns-excluded` collection of symbols of the namespaces not to analyze
  * `conf-report-output-kw` keyword under the configuration key `[:doc :reports]` which is telling the name of file
  * `assert-message` display this error message if the report is not empty
  * `report-title` save the report under this file name
  * `filter-repo-with` keyword of the type of files you filter the code-files-repo with. See [file-is?](automaton-build-app.code-helpers.clj-code/file-is?) for details"
    {::css {:pattern #":class\s*\"|:(a|abbr|acronym|address|applet|area|article|aside|audio|b|automaton|basefont|bdi|bdo|big|blockquote|body|br|button|canvas|caption|center|cite|code|col|colgroup|data|datalist|dd|del|details|dfn|dialog|dir|div|dl|dt|em|embed|fieldset|figcaption|figure|font|footer|form|frame|frameset|h1|h2|h3|h4|h5|head|header|hr|html|i|iframe|img|input|ins|kbd|label|legend|li|link|main|map|mark|meta|meter|nav|noframes|noscript|object|ol|optgroup|option|output|p|param|picture|pre|progress|q|rp|rt|ruby|s|samp|script|section|select|small|source|span|strike|strong|style|sub|summary|sup|svg|table|tbody|td|template|textarea|tfoot|th|thead|time|title|tr|track|tt|u|ul|var|video|wbr)(?:\#|\.)"
            :ns-excluded ['automaton-build-app.code-helpers.test-toolings]
            :pattern-match-to-report (fn [filename [_whole-match namespace alias _]]
                                       [namespace filename alias])
            :conf-report-output-kw :css
            :assert-message "Found forbidden css in the code"
            :report-title "List of forbidden css forms"}
     ::one-alias-per-ns {:filter-repo-with :code}
     ::alias {:pattern #"^\s*\[\s*([A-Za-z0-9\*\+\!\-\_\.\'\?<>=]*)\s*(?:(?::as)\s*([A-Za-z0-9\*\+\!\-\_\.\'\?<>=]*)|(:refer).*)\s*\]\s*(?:\)\))*$"
              :pattern-match-to-report (fn [filename [_whole-match comment]]
                                         [])}}))
