(ns automaton-build-app.utils.map-test
  (:require [automaton-build-app.utils.map :as sut]
            [clojure.test :refer [deftest is testing]]))

(deftest sort-submap-test
  (testing "Are keywords first, symbol then and each sorted alphabetically"
    ; For a reason I can't understand this is not comparable, do you have
    ; any idea why, look at cider it's weird
    (is (= (hash {:sk {:a 4
                       :z 2
                       'aa 3
                       'foo 1}})
           (hash (sut/sort-submap {:sk {'foo 1
                                        :z 2
                                        'aa 3
                                        :a 4}}
                                  [:sk])))))
  (testing "Edge cases"
    (is (= {:sk {}} (sut/sort-submap {} [:sk])))
    (is (= {:sk {:foo :bar
                 :a :b
                 :sl {}}
            :sl {'bar :foo
                 'aa :bb}}
           (sut/sort-submap {:sk {:foo :bar
                                  :a :b}
                             :sl {'bar :foo
                                  'aa :bb}}
                            [:sk :sl])))
    (is (= {} (sut/sort-submap {})))))
