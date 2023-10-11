(ns automaton-build-app.containers
  "Defines container expectations")

(defprotocol Container
  (build [this publish?] "Build the github action image\n Params:\n * `publish?` if true publish the image\n Return true or nil if any error")
  (connect [this] "Connect to a local copy of that container")
  (container-name [this] "name for the local engine and remote repo"))
