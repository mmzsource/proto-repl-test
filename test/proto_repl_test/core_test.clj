(ns proto-repl-test.core-test
  (:require [clojure.test :refer :all]
            [proto-repl-test.core :refer :all]))

(deftest wpm-should-filter-nil-positions
  (testing "should zipmap inputs and filter nil positions"
    (let [weights   [2 3 1]
          positions [5 nil 11]
          weight-position-seq (weight-position-seq weights positions)]
      (is (= weight-position-seq [[2 5] [1 11]])))))

(deftest calc-avg-pos-should-calculate-average-position
 (testing "should calculate average position based on weight-position-map"
   (let [wps1 '([2 5] [1 11])]
     (is (= (calc-avg-pos '([2 5] [1 11])) 7)))))
