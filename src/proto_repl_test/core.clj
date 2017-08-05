(ns proto-repl-test.core
  (:require [prc :as charts]))

(defn -main [& args]
  (println "Hello, World! Testing proto-repl & proto-charts"))

;; Bar chart

(defn roll-die [num-sides]
  (inc (rand-int num-sides)))

(charts/bar-chart "Six sided dice frequencies"
  (frequencies (repeatedly 1000 #(roll-die 6))))

(defn roll-dice [num-sides num-dice]
  (reduce + (repeatedly num-dice #(roll-die num-sides))))

(charts/bar-chart "Sum of 2 dices"
  (frequencies (repeatedly 1000 #(roll-dice 6 2))))

(charts/bar-chart "It's just data! (strings as key)" {"ABC" 10 "DEF" 12 "GHI" 7})

(charts/bar-chart "It's just data! (keywords as key)" {:a 10 :b 12 :c 7})

(charts/bar-chart "It's just data! (integers as key)" {1 10 2 12 3 7})

;; Scatter chart

(charts/scatter-chart "Dice scatter"
  {:six    (repeatedly 1000 #(roll-dice 6 2))
   :ten    (repeatedly 1000 #(roll-dice 10 3))
   :twenty (repeatedly 1000 #(roll-dice 20 4))})

;; Graphs

(charts/graph "A graph test"
  {:nodes [:a :b :c]
   :edges [[:a :b] [:a :c]]})

(def parents->children {["Rene" "Ricky"] ["Robin" "Maarten" "Jeroen"]
                        ["Paul" "Inge"]  ["Paul jr" "Irene"]
                        ["Maarten" "Irene"] ["Wessel" "Casper"]
                        ["Jeroen" "Rebekka"] ["Thomas" "Benthe"]
                        ["Robin" "Monique"] ["Romy" "Mike"]
                        ["Paul jr" "Nausika"] ["Anne" "Lennart"]})

(charts/graph "Family tree"
 {:nodes (distinct (flatten (seq parents->children)))
  :edges (mapcat #(for [parent (key %)
                        child  (val %)]
                     [parent child])
            parents->children)})

(charts/graph "Tweaked Family tree"
 {:nodes (distinct (flatten (seq parents->children)))
  :edges (mapcat #(for [parent (key %)
                        child  (val %)]
                     [parent child])
            parents->children)}
 {:nodes {:font {:size 18}}
  :edges {:arrows {:to   {:scaleFactor 0.5}
                   :from {:scaleFactor 0.5}}}
  :layout {:hierarchical {:enabled true
                          :levelSeparation 150
                          :direction "LR" ;; left-right. Also try up-down "UD"
                          :sortMethod "directed"}}})

;; Timeseries

;; Would normally be slurped in:
(def mocked-csv-data
  "Date,High,Low,Volume
2017-01-01,10.8,9.2,150234
2017-01-04,12.2,10.8,234122
2017-01-05,12.0,8.5,244983")

(def row-data (partition 4 (clojure.string/split mocked-csv-data #"[,\n]")))

(charts/custom-chart "Timeseries"
  {:data {:rows row-data
          :x "Date"
          :axes {:Volume "y2"}}
   :axis {:x {:type "timeseries"
              :tick {:format "%Y-%m-%d"}}
          :y2 {:show true}}})
