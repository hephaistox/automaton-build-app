(ns automaton-build-app.doc.blog-test
  (:require [automaton-build-app.doc.blog :as sut]
            [clojure.test :refer [deftest is testing]]))

(deftest customer-materials-dir-test
  (testing "Customer materials dir"
    (is (string? (sut/customer-materials-dir)))))

(comment
  (sut/configuration-data "../../../docs/customer_materials/elevator/elevator.edn"
                          "../../../tmp/html"
                          "../../../tmp/pdf")

  (-> (sut/configuration-data "../../../docs/customer_materials/elevator/elevator.edn"
                              "../../../tmp/html"
                              "../../../tmp/pdf")
      first
      sut/configuration-data-by-language-to-html-pdf)

  (sut/blog-process "../tmp")
;
  )
