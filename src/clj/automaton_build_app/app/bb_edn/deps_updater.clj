(ns automaton-build-app.app.bb-edn.deps-updater "Update the deps of the bb-edn")

(defn update-bb-deps
  "Copy the `:extra-deps` of the aliases `:bb-deps` into the `bb.edn`
  Params:
  * `app`"
  [app]
  (let [bb-deps (get-in app [:deps-edn :aliases :bb-deps :extra-deps])] (update app :bb-edn #(assoc % :deps bb-deps))))
