(ns proto-repl-test.core-test
  (:require [clojure.test :refer :all]
            [proto-repl-test.core :refer :all]))


(deftest should-filter-nil-y-vals
  (testing "should zipmap inputs and filter nil positions"
    (let [weights   [2 3 1]
          positions [5 nil 11]
          weight-positions (filtered-xy weights positions)]
      (is (= weight-positions [[2 5] [1 11]])))))


(deftest calc-avg-pos-should-calculate-average-position
 (testing "should calculate average position based on weight-position-map"
   (is (= (calc-avg-pos [2 3 1] [5 nil 11])) 7)))


(deftest transform-data-transforms-correctly
  (testing "should transform raw data correctly"
    (let [raw-data {:rows [[:PSV "PSV"  1 1 4 3 3 3 2 4 1 1 3]
                           [:AJA "Ajax" 2 2 3 2 1 1 1 1 2 2 2]]}
          expected {:PSV [1 1 4 3 3 3 2 4 1 1 3]
                    :AJA [2 2 3 2 1 1 1 1 2 2 2]}]
      (is (= expected (transform-raw-data raw-data))))))


(deftest team-averages-calculates-team-averages-correctly
  (testing "should calculate team averages correctly"
    (let [weights   {:horizontal [0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5]
                     :ascend     [0.001 0.003 0.014 0.034 0.065 0.112 0.178 0.279 0.443 0.938]}
          team-data [:PSV "PSV" [1 1 4 3 3 3 2 4 1 1 3]]
          expected  {:team       :PSV,
                     :history    [1 1 4 3 3 3 2 4 1 1],
                     :last-pos   3,
                     :horizontal 2.3,
                     :ascend     1.7155297532656022}]
      (is (= expected (team-averages weights team-data))))))


(deftest team-averages-handles-nil-correctly
  (testing "team averages handles nil correctly"
    (let [weights   {:horizontal [0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5]
                     :ascend     [0.001 0.003 0.014 0.034 0.065 0.112 0.178 0.279 0.443 0.938]}
          team-data [:JPS "Jong PSV" [nil nil nil nil nil nil nil 28 32 29 22]]
          expected  {:team       :JPS,
                     :history    [nil nil nil nil nil nil nil 28 32 29],
                     :last-pos   22,
                     :horizontal 29.666666666666668,
                     :ascend     29.632530120481928}]
      (is (= expected (team-averages weights team-data))))))


(deftest finds-best-prediction
  (testing "finds best prediction"
    (let [predictor-keys [:pred1 :pred2 :pred3 :pred4]
          predictions    {:team     :PSV,
                          :last-pos 3,
                          :pred1    2.3,
                          :pred2    3.1,
                          :pred3    4.6,
                          :pred4    2.8
                          :foo     "bar"}]
      (is (= {:PSV :pred2} (find-best-predictor predictor-keys predictions))))))


(deftest predicts-new-positions
  (testing "predicts new positions based on best predictors"
    (let [league-data     {:PSV [1 1 4 3 3 3 2 4 1 1 3] :AJA [2 2 3 2 1 1 1 1 2 2 2]}
          weights         {:horizontal [0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5]
                           :ascend     [0.001 0.003 0.014 0.034 0.065 0.112 0.178 0.279 0.443 0.938]}
          best-predictors {:PSV :ascend :AJA :horizontal}]
      (is (= {:PSV 2.333817126269956 :AJA 1.7} (predict-new-positions league-data weights best-predictors))))))


(deftest calculates-lin-reg-vals1
  (testing "calculates linear regression y values for each x value"
     (let [xs [1.0 2.0 3.0 4.0 5.0]
           ys [2.0 2.5 3.0 3.5 4.0]]
       (is (= [2.0 2.5 3.0 3.5 4.0] (linreg-vals xs ys))))))


(deftest calculates-lin-reg-vals2
  (testing "calculates linear regression y values for each x value"
     (let [xs [1.0 2.0 3.0 4.0 5.0]
           ys [2.0 2.5 2.5 3.5 4.0]]
       (is (= [1.9 2.4 2.9 3.4 3.9] (linreg-vals xs ys))))))

(deftest calculates-lin-reg-fn
  (testing "calculates linear regression function which extrapolates correctly"
     (let [xs [1.0 2.0 3.0 4.0 5.0]
           ys [2.0 2.5 3.0 3.5 4.0]]
       (is (= 1.5 ((linreg-fn xs ys) 0.0)))
       (is (= 4.5 ((linreg-fn xs ys) 6.0))))))
