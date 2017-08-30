(ns proto-repl-test.core
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.pprint :as pp]
            [proto-repl-test.team :as team]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;          Input data           ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn load-a-file [filename]
  (io/file (io/resource filename)))


(defn load-raw-data [file]
  (edn/read-string (slurp file)))


(defn transform-weights-data [raw-data]
  (->> (:rows raw-data)
       (map #(hash-map (first %) (subvec % 2)))
       (reduce merge)))

(def weights-data
  (transform-weights-data (load-raw-data (load-a-file "weights.edn"))))


(defn construct-teams [raw-league-data]
  (map
    (fn [[kw name & history]]
      (team/construct kw name (vec history)))
    (:rows raw-league-data)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;     Position calculation with weight-curves     ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn check-xys [xs ys]
  (assert seq? xs)
  (assert seq? ys)
  (assert (> (count xs) 0))
  (assert (= (count xs) (count ys)))
  (assert (every? number? xs))
  (assert (every? #(or (number? %) (nil? %)) ys)))

(defn xy-nil-filter
  "Returns a sequence of [x y] pairs with no nil values.
   Expects a sequence of xs and a sequence of ys.
   mapv's the items in sequences and removes the items where ys are nil."
  [xs ys]
  (check-xys xs ys)
  (let [xys (mapv vector xs ys)]
    (filter #(not (nil? (last %))) xys)))


(defn weighted-avg
  "Expects a sequence of weights and a sequence of values,
   Removes nils and calculates average weighted value:
   sum of (weight * value) / sum of weights"
  [ws vs]
  (check-xys ws vs)
  (let [wvs (xy-nil-filter ws vs)
        avg (/ (reduce + (map #(let [[w v] %] (* w v)) wvs))
               (reduce + (map first wvs)))]
    (double avg)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Position calculation with linear regression ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn mean [numbers]
  (/ (apply + numbers) (count numbers)))


(defn square [x] (* x x))


(defn b1 [xs ys]
  (/
    (apply +
           (map *
               (map (fn [v] (- v (mean xs))) xs)
               (map (fn [v] (- v (mean ys))) ys)))
    (apply +
           (map (fn [v] (square (- v (mean xs)))) xs))))


(defn b0 [xs ys b1]
  (- (mean ys)
     (* b1 (mean xs))))


(defn linreg-vals
  "Return calculated regression values for all xs"
  [xs ys]
  (let [b1 (b1 xs ys)
        b0 (b0 xs ys b1)]
    (into [] (map (fn [x-val] (+ (* b1 x-val) b0)) xs))))


(defn linreg-fn
  "Returns linear regression function"
  [xs ys]
  (let [b1 (b1 xs ys)
        b0 (b0 xs ys b1)]
    (fn [x] (+ b0 (* b1 x)))))


(defn linear-regression-prediction
  "Returns the next predicted value based on a sequence of input values."
  [vs]

  (assert seq? vs)
  (assert (> (count vs) 1))
  (assert (every? #(or (number? %) (nil? %)) vs))

  (let [count           (inc (count vs))
        xys             (xy-nil-filter (range 1 count) vs)
        xs              (map #(first %)  xys)
        ys              (map #(second %) xys)
        linreg-function (linreg-fn xs ys)]
    (double (linreg-function count))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;                  Predictions                 ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn predict
  "Calculates last years position based on historic data
   Uses weighted averages and linear regression as algorithms.
   This is where you should 'plugin' new predictions"
  [team]
  (let [all-but-last-position (team/all-but-last-position team)]
    (merge
      {:hor (weighted-avg (:hor weights-data) all-but-last-position)}
      {:das (weighted-avg (:das weights-data) all-but-last-position)}
      {:las (weighted-avg (:las weights-data) all-but-last-position)}
      {:ias (weighted-avg (:ias weights-data) all-but-last-position)}
      {:slr (linear-regression-prediction all-but-last-position)})))


(defn sort-best-predictors
  "should sort predictors given a map of predictions and
   and an optimal value"
  [predictions optimum]
  (let [diff (zipmap
               (keys predictions)
               (map #(Math/abs (- optimum %))
                 (vals predictions)))]
    (sort-by val < diff)))


(defn find-best-predictor
  "should return best predictor given a map of predictions and
   and an optimal value"
  [predictions optimum]
  (key (first (sort-best-predictors predictions optimum))))


(defn analyse [team]
  (let [team-key           (team/team-key team)
        history            (team/history team)
        last-team-position (team/last-position team)
        predictions        (predict team)
        best-predictor     (find-best-predictor predictions last-team-position)
        second-best-pred   (key (second (sort-best-predictors predictions last-team-position)))
        prediction         (if (= best-predictor :slr)
                             (linear-regression-prediction history)
                             (weighted-avg
                               (best-predictor weights-data)
                               (rest history)))]
    {:team team-key, :history history, :last-pos last-team-position,
     :predictions predictions, :best best-predictor,
     :2nd-best second-best-pred :prediction prediction}))


(defn predict-ranks [analysis team-filter]
  (let [rank-data       (reduce
                          (fn [acc t]
                            (assoc acc (:team t) (:prediction t)))
                          {}
                          analysis)
        filtered-ranks (select-keys rank-data team-filter)]
    (sort-by val < filtered-ranks)))

(defn print-summary [analysis predict-ranks]
  (let [easy-lookup    (reduce (fn [acc t] (assoc acc (:team t) t)) {} analysis)
        numbered-ranks (sort-by key < (zipmap (range 1 21) (map first predict-ranks)))
        a-rank         (first numbered-ranks)
        team-summary   (let [rank (first a-rank)
                             team-keyword (second a-rank)
                             team (team-keyword easy-lookup)]
                         (hash-map
                           :pos          rank
                           :team         team-keyword
                           :pred         (format "%.3f" (:prediction team))
                           :hist         (:history team)
                           :best         (:best team)
                           :best-val     (format "%.3f" ((:best team) (:predictions team)))
                           :2nd-best     (:2nd-best team)
                           :2nd-best-val (format "%.3f" ((:2nd-best team) (:predictions team)))))]
    (pp/pprint easy-lookup)
    (pp/pprint numbered-ranks)
    (pp/print-table
      [:pos :team :pred :hist :best :best-val :2nd-best :2nd-best-val]
      [team-summary])))

(defn -main [& args]
  (let [raw-league-data (load-raw-data
                          (load-a-file (or (first args) "NL.edn")))
        team-data       (construct-teams raw-league-data)
        analysis        (map analyse team-data)
        predict-ranks   (predict-ranks analysis (:teams raw-league-data))]
    (print-summary analysis predict-ranks)))
