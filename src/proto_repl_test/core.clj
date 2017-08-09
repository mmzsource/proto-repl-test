(ns proto-repl-test.core
  (:require [prc :as charts]
            [clojure.java.io :as io]
            [clojure.edn :as edn]))

(defn -main []
  (prn "Prints predicted ranks based on (weighted) historic rank data"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;          Input data           ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def raw-league-data
  (edn/read-string
    (slurp (io/resource "NL-eindstanden-normalized.edn"))))

(def raw-weights-data
  (edn/read-string
    (slurp (io/resource "weights.edn"))))

(defn transform-raw-data [raw-data]
  (->> (:rows raw-data)
       (map #(hash-map (first %) (subvec % 2)))
       (reduce merge)))

(def league-data (transform-raw-data raw-league-data))

(def weights-data (transform-raw-data raw-weights-data))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;     Visualise input data     ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(charts/scatter-chart "League Data" league-data)

(charts/scatter-chart "Weights Data" weights-data)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Average position calculation ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn weight-position-seq
  "Expects a vector of weights and a vector of positions,
   mapv's the items in vectors and removes the items where position data is nil."
  [weights position]
  (let [wp (mapv vector weights position)]
    (filter #(not (nil? (last %))) wp)))

(defn calc-avg-pos
  "Expects a map with weights as keys and positions as value.
   Calculates average position based on that data:
   sum of (weight * year position) / sum of weights"
  [weight-position-seq]
  (/ (reduce + (map #(let [[w p] %] (* w p)) weight-position-seq))
     (reduce + (map first weight-position-seq))))

(defn team-averages [team-data]
  (let [team-keyword  (first team-data)
        team-history  (subvec team-data 2 12)
        last-team-pos (last team-data)]
    (merge
      {:team     team-keyword
       :history  team-history
       :last-pos last-team-pos}
      (reduce merge
        (map #(hash-map
                %
                (calc-avg-pos (weight-position-seq (% weights-data) team-history)))
           (keys weights-data))))))

(map team-averages (:rows raw-league-data))

;; Finding the keys of the best 3 predictions:

(def m {:p1 5 :p2 7 :p3 9 :p4 2 :p5 8 :lp 6 :na "not applicable"})

(def fm (select-keys m [:p1 :p2 :p3 :p4 :p5]))

(def diff-fm (zipmap (keys fm) (map #(Math/abs (- (:lp m) %)) (vals fm))))

(keys (take 3 (sort-by val < diff-fm)))
