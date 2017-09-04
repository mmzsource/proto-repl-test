(ns soc-pred-17.core-test
  (:require [clojure.test :refer :all]
            [soc-pred-17.core :refer :all]))


(deftest should-filter-nil-y-vals
  (testing "should zipmap inputs and filter nil positions"
    (let [xs  [2 3 1]
          ys  [5 nil 11]
          xys (xy-nil-filter xs ys)]
      (is (= xys [[2 5] [1 11]])))))

(deftest filtered-xy-should-accept-lists
  (testing "should accept lists"
    (let [xs  '(5 3 1)
          ys  '(nil 2 11)
          xys (xy-nil-filter xs ys)]
      (is (= xys [[3 2] [1 11]])))))

(deftest should-calculate-weighted-average
 (testing "should calculate weighted average"
   (is (= (weighted-avg [2 3 1] [5 nil 11])) 7.0)))


(deftest weighted-avg-should-accept-lists
 (testing "should calculate weighted average"
   (is (= (weighted-avg '(1 2 1) '(3 2 1)) 2.0))))

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

(deftest calculated-linear-regression-prediction
  (testing "calculates next value with linear regression given a sequence of values"
    (let [vs [1 2 3 4 5]]
      (is (= 6.0 (linear-regression-prediction [1 2 3 4 5])))
      (is (= 0.0 (linear-regression-prediction [5 4 nil 2 1]))))))

(deftest predict-team
  (testing "predicts next years team position with various algorithms"
    (let [team     [:PSV "PSV" [1 1 4 3 3 3 2 4 1 1 3]]
          expected {:das 2.4,
                    :ias 1.7,
                    :las 2.2,
                    :hor 2.3,
                    :slr 2.2}
          result   (predict team)]
      (is (= expected result)))))

(deftest predict-team
  (testing "predict can handle null values"
    (let [team     [:BLA "bla" [3 3 nil 3 3 nil nil 3 3 nil 3]]
          expected {:das 3.0000000000000004,
                    :ias 3.0000000000000004,
                    :las 2.9999999999999996,
                    :hor 3.0,
                    :slr 3.0}
          result   (predict team)]
      (is (= expected result)))))

(deftest should-find-best-predictor
  (testing "should return best predictor given a map of predictions and
            and an optimal value"
    (let [predictions {:p1 5 :p2 6 :p3 7}]
      (is (= :p1 (find-best-predictor predictions 4)))
      (is (= :p3 (find-best-predictor predictions 8)))
      (is (= :p2 (find-best-predictor predictions 6))))))

(deftest should-analyse-a-team
  (testing "should analyse a team completely"
    (let [team     [:PSV "PSV" [1 1 4 3 3 3 2 4 1 1 3]]
          expected {:team :PSV,
                    :name "PSV",
                    :history [1 1 4 3 3 3 2 4 1 1 3],
                    :last-pos 3,
                    :best :das
                    :2nd-best :hor,
                    :final-predictor :das,
                    :prediction 2.5585528803731252,
                    :predictions {:hor 2.3, :das 2.434135888062524, :las 2.266986410871303, :ias 1.7155297532656022, :slr 2.2},}]

      (is (= expected (analyse team))))))
