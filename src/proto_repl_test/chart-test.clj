(ns proto-repl-test.chart-test
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

(proto-repl-charts.charts/bar-chart
  "GDP_By_Year"
  {"2013" [16768 9469 4919 3731]
   "2014" [17418 10380 4616 3859]}
  {:labels ["US" "China" "Japan" "Germany"]})

;; Scatter chart

(charts/scatter-chart "Dice scatter"
  {:six    (repeatedly 1000 #(roll-dice 6 2))
   :ten    (repeatedly 1000 #(roll-dice 10 3))
   :twenty (repeatedly 1000 #(roll-dice 20 4))})

;; Graphs

(charts/graph "A graph test"
  {:nodes [:a :b :c]
   :edges [[:a :b] [:a :c]]})

(proto-repl-charts.graph/graph
  "A Graph from a map"
  {:nodes [:a :b :c :d :e :f :g]
   :edges [[:d :b] [:b :c] [:d :c] [:e :f] [:f :g]
           [:d :a] [:f :c]]})

;; Groups will be given unique colors
(let [nodes [{:id 0 :label "0" :group 0}
             {:id 1 :label "1" :group 0}
             {:id 2 :label "2" :group 0}
             {:id 3 :label "3" :group 1}
             {:id 4 :label "4" :group 1}
             {:id 5 :label "5" :group 1}
             {:id 6 :label "6" :group 2}
             {:id 7 :label "7" :group 2}
             {:id 8 :label "8" :group 2}
             {:id 9 :label "9" :group 3}
             {:id 10 :label "10" :group 3}
             {:id 11 :label "11" :group 3}
             {:id 12 :label "12" :group 4}
             {:id 13 :label "13" :group 4}
             {:id 14 :label "14" :group 4}
             {:id 15 :label "15" :group 5}
             {:id 16 :label "16" :group 5}
             {:id 17 :label "17" :group 5}
             {:id 18 :label "18" :group 6}
             {:id 19 :label "19" :group 6}
             {:id 20 :label "20" :group 6}
             {:id 21 :label "21" :group 7}
             {:id 22 :label "22" :group 7}
             {:id 23 :label "23" :group 7}
             {:id 24 :label "24" :group 8}
             {:id 25 :label "25" :group 8}
             {:id 26 :label "26" :group 8}
             {:id 27 :label "27" :group 9}
             {:id 28 :label "28" :group 9}
             {:id 29 :label "29" :group 9}]
      edges [[1 0] [2 0] [4 3] [5 4] [4 0] [7 6] [8 7] [7 0] [10 9] [11 10]
             [10 4] [13 12] [14 13] [13 0] [16 15] [17 15] [15 10] [19 18]
             [20 19] [19 4] [22 21] [23 22] [22 13] [25 24] [26 25] [25 7]
             [28 27] [29 28] [28 0]]]
  (proto-repl-charts.graph/graph
    "Custom Graph"
    {:nodes nodes :edges edges}
    ;; Options
    {:nodes {:shape "dot" :size 30 :font {:size 32 :color "#111111"}
             :borderWidth 2}
     :edges {:width 2}}))

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

(require '[loom.graph :as lg])
(let [graph (lg/graph [1 0] [2 0] [4 0] [3 4] [5 4] [10 4] [9 10] [11 10]
                      [15 10] [17 15] [15 16] [7 0] [6 7] [8 6])]
  (proto-repl-charts.graph/graph "Loom Graph" graph))

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

;; Line charts

(proto-repl-charts.charts/line-chart
  "Trigonometry"
  {"sin" (map #(Math/sin %) (range 0.0 6.0 0.2))
   "cos" (map #(Math/cos %) (range 0.0 6.0 0.2))})

(let [input-values (range 0.0 6.0 0.5)]
  (proto-repl-charts.charts/line-chart
   "Trigonometry"
   {"sin" (map #(Math/sin %) input-values)
    "cos" (map #(Math/cos %) input-values)}
   {:labels input-values}))

;; Custom Chart

(proto-repl-charts.charts/custom-chart
  "Custom"
  {:data {:columns
          [["data1" 30 20 50 40 60 50]
           ["data2" 200 130 90 240 130 220]
           ["data3" 300 200 160 400 250 250]
           ["data4" 200 130 90 240 130 220]
           ["data5" 130 120 150 140 160 150]
           ["data6" 90 70 20 50 60 120]]
          :type "bar"
          :types {:data3 "spline"
                  :data4 "line"
                  :data6 "area"}
          :groups [["data1" "data2"]]}})

;; Table

(proto-repl-charts.table/table
  "Users"
  [{:name "Jane" :age 24 :favorite-color :blue}
   {:name "Matt" :age 28 :favorite-color :red}
   {:name "Austin" :age 56 :favorite-color :green}
   {:name "Lisa" :age 32 :favorite-color :green}
   {:name "Peter" :age 32 :favorite-color :green}])
