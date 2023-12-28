(ns automaton-build-app.data-structure.graph.apps-stub)

#_{:clj-kondo/ignore [:unresolved-namespace]}
(def apps-w-deps-stub
  [{:app-dir "ldir/app_stub"
    :build-config {:monorepo {:app-dir "app_stub"}
                   :app-name "app-stub"
                   :publication {:as-lib 'hephaistox/app-stub}}
    :deps-edn {:deps {'babashka/process {}
                      'hephaistox/automaton {}
                      'hephaistox/build {}}}}
   {:app-dir "ldir/everything"
    :build-config {:monorepo {:app-dir "everything"}
                   :app-name "everything"
                   :publication {:as-lib 'hephaistox/everything}}
    :deps-edn {:deps {}}}
   {:build-config {:app-dir "ldir/base_app"
                   :publication {:as-lib 'hephaistox/base-app}
                   :app-name "base-app"
                   :monorepo {:app-dir "base_app"}}
    :deps-edn {:deps {'hephaistox/build {}}}}
   {:build-config {:monorepo {:app-dir "build"}
                   :app-dir "ldir/build_app"
                   :app-name "build"
                   :publication {:as-lib 'hephaistox/build}}
    :deps-edn {:deps {}}}])
