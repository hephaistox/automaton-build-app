(ns automaton-build-app.utils.uuid-gen-test
  (:require [automaton-build-app.utils.uuid-gen :as sut]
            [clojure.test :refer [testing deftest is]]))

(deftest unguessable
  (testing "check that generates proper uuid"
    (is (every? uuid?
                (repeatedly 10 #(sut/time-based-uuid))))))
