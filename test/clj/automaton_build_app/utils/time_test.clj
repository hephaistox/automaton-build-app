(ns automaton-build-app.utils.time-test
  (:require
   [automaton-build-app.utils.time :as sut]
   [clojure.test :refer [deftest is testing]]))

(deftest now-str
  (testing "Check date is generated"
    (let [date-str (sut/now-str)]
      (is (string? date-str))
      (is (> (count date-str)
             20)))))
