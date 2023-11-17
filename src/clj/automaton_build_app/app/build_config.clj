(ns automaton-build-app.app.build-config
  "Manage `build-config.edn` file"
  (:require [automaton-build-app.os.edn-utils :as build-edn-utils]
            [automaton-build-app.os.files :as build-files]
            [automaton-build-app.schema :as build-schema]))

(def build-config-filename "build_config.edn")

(def build-config-schema
  "Build config schema"
  [:map {:closed true} [:app-name :string] [:build? {:optional true} :boolean] [:cust-app? {:optional true} :boolean]
   [:everything? {:optional true} :boolean] [:template-app? {:optional true} :boolean] [:doc? {:optional true} :boolean]
   [:frontend? {:optional true} :boolean]
   ;; This map is not closed, as monorepo features should not
   ;; be described here that data are here for convenience
   [:publication
    [:map {:closed true} [:repo {:optional true} [:map {:closed true} [:address :string] [:branch :string]]]
     [:as-lib {:optional true} :symbol] [:major-version {:optional true} :string]
     [:gha-container {:optional true}
      [:map {:closed true} [:repo-url :string] [:repo-branch :string] [:account :string] [:workflows [:vector :string]]]]
     [:shadow-cljs {:optional true} [:map {:closed true} [:target-build [:maybe :keyword]]]]
     [:jar {:optional true} [:map {:closed true} [:class-dir :string] [:excluded-aliases [:set :keyword]] [:target-filename :string]]]]]
   [:lconnect {:optional true} [:map {:closed true} [:aliases [:vector :keyword]]]]
   [:format-code {:optional true} [:map {:closed true} [:exclude-dirs [:set :string]]]]
   [:ltest {:optional true} [:map {:closed true} [:aliases [:vector :keyword]]]] [:la {:optional true} [:map {:closed true}]]
   [:bb-tasks {:optional true}
    [:map {:closed true} [:exclude-tasks {:optional true} [:set :symbol]] [:select-tasks {:optional true} [:vector :string]]]]
   [:customer-materials {:optional true} [:map {:closed true} [:html-dir :string] [:dir :string] [:pdf-dir :string]]]
   [:monorepo
    {:optional true
     :closed false} :map] [:container-repo {:optional true} [:map {:closed true} [:account :string]]]
   [:doc {:optional true}
    [:map {:closed true} [:dir :string] [:archi [:map {:closed true} [:dir :string]]] [:exclude-dirs {:optional true} [:set :string]]
     [:reports [:map {:closed true} [:output-files [:map-of :keyword :string]] [:forbidden-words [:set :string]]]]
     [:code-stats [:map {:closed true} [:output-file :string]]]
     [:code-doc {:optional true} [:map {:closed true} [:dir :string] [:title :string] [:description :string]]]]]
   [:clean [:map {:closed true} [:compile-logs-dirs [:vector :string]]]]
   [:templating {:optional true} [:map {:closed true} [:app-title :string]]]])

(defn valid?
  "Validate the file build config matches the expected format
  Return false if not validated,
  Returns `app-build-config if validated``
  Params:
  * `app-build-config` content of the file to validate"
  [app-build-config]
  (build-schema/valid? build-config-schema app-build-config))

(defn spit-build-config
  "Spit a build config file
  Params:
  * `app-dir` where to store the build_config file
  * `content` to spit
  * `msg` (optional) to add on the top of the file"
  ([app-dir content msg]
   (let [filename (build-files/create-file-path app-dir build-config-filename)]
     (build-edn-utils/spit-edn filename content msg)
     filename))
  ([app-dir content] (spit-build-config app-dir content nil)))

(defn read-build-config
  "Load the `build_config.edn` file of an app
  Params:
  * `app-dir` root directory of the app where the `build_config.edn` file is expected"
  [app-dir]
  (some-> (build-files/create-file-path app-dir build-config-filename)
          build-files/is-existing-file?
          build-edn-utils/read-edn
          ;;It is made on purpose to still use the build_config.edn even if it
          ;;is not validated.
          valid?))
