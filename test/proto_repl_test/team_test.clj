(ns proto-repl-test.team-test
  (:require [clojure.test :refer :all]
            [proto-repl-test.team :refer :all]))


(deftest should-hide-team-structure
  (testing "should hide team structure"
    (let [my-team [:BLA "blabla" [1 2 3 4 5]]]
      (is (= :BLA        (team-key my-team)))
      (is (= "blabla"    (team-name my-team)))
      (is (= [1 2 3 4 5] (history my-team)))
      (is (= 5           (last-position my-team)))
      (is (= [1 2 3 4]   (all-but-last-position my-team))))))
