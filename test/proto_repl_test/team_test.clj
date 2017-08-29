(ns proto-repl-test.team-test
  (:require [clojure.test :refer :all]
            [proto-repl-test.team :refer :all]))


(deftest should-construct-a-team
  (testing "the construction of a team datatype"
    (is (= [:FIETS "bel" [9 8 7 6]] (construct :FIETS "bel" [9 8 7 6])))))


(deftest should-hide-team-structure
  (testing "should hide team structure"
    (let [my-team [:BLA "blabla" [1 2 3 4 5]]]
      (is (= :BLA        (team-key my-team)))
      (is (= "blabla"    (team-name my-team)))
      (is (= [1 2 3 4 5] (history my-team)))
      (is (= 5           (last-position my-team)))
      (is (= [1 2 3 4]   (all-but-last-position my-team))))))
