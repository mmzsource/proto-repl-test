(ns soc-pred-17.core
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.pprint :as pp]
            [soc-pred-17.team :as team]))


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
  (assert pos? (count xs))
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
    (vec (map (fn [x-val] (+ (* b1 x-val) b0)) xs))))


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
        xs              (map first  xys)
        ys              (map second xys)
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

(defn predict-new-position [best-predictor second-best-pred history]
  (let [nils (count (filter nil? history))]
    (if (= best-predictor :slr)
      (let [linreg-pred     (linear-regression-prediction history)
            non-nil-seq     (remove nil? history)
            last-pos        (last non-nil-seq)
            weighted-pos    (double (/ (+ (linear-regression-prediction (drop-last history)) last-pos) 2))
            one-to-last-pos (second (reverse non-nil-seq))]
            ;weighted-pos    (double (/ (+ one-to-last-pos (* 2 last-pos)) 3))]
        (if (or (> nils 4) (< linreg-pred 6))
            ; override if not enough data (too much nils) or
            ; linear regression would predict a team into the top of the league
            ; I think lin reg doesn't hold for the top of the league
            ; In those cases take the lin reg prediction (without last years rank)
            ; and average it with last years rank.
            [weighted-pos :ovr]
            [linreg-pred :slr]))
      [(weighted-avg (best-predictor weights-data) (rest history)) best-predictor])))

(defn analyse [team]
  (let [team-key           (team/team-key team)
        team-name          (team/team-name team)
        history            (team/history team)
        last-team-position (team/last-position team)
        predictions        (predict team)
        best-predictor     (find-best-predictor predictions last-team-position)
        second-best-pred   (key (second (sort-best-predictors predictions last-team-position)))
        pred-tuple         (predict-new-position best-predictor second-best-pred history)
        prediction         (first pred-tuple)
        predictor          (second pred-tuple)]
    {:team team-key, :name team-name,
     :history history, :last-pos last-team-position,
     :predictions predictions, :best best-predictor,
     :2nd-best second-best-pred :prediction prediction
     :final-predictor predictor}))


(defn predict-ranks [analysis team-filter]
  (let [rank-data       (reduce
                          (fn [acc t]
                            (assoc acc (:team t) (:prediction t)))
                          {}
                          analysis)
        filtered-ranks (select-keys rank-data team-filter)]
    (sort-by val < filtered-ranks)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;                  Pretty printing             ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn team-summary [rank team-keyword team-data]
  (hash-map
    :pos    rank
    :team   team-keyword
    :name   (:name team-data)
    :pred   (format "%.3f"
              (:prediction team-data))
    :hist   (:history team-data)
    :best-p (:best team-data)
    :best-v (format "%.3f"
              ((:best team-data) (:predictions team-data)))
    :2nd-p  (:2nd-best team-data)
    :2nd-v  (format "%.3f"
              ((:2nd-best team-data) (:predictions team-data)))
    :f-pred (:final-predictor team-data)))


(defn print-summary [analysis predict-ranks]
  (let [easy-lookup    (reduce (fn [acc t] (assoc acc (:team t) t)) {} analysis)
        numbered-ranks (sort-by key < (zipmap (range 1 21) (map first predict-ranks)))
        league-summary (map
                         #(team-summary (first %) (second %) ((second %) easy-lookup))
                         numbered-ranks)
        best-keys-freq (frequencies (map :best-p league-summary))
        sec-best-freq  (frequencies (map :2nd-p  league-summary))]
    (pp/print-table
      [:pos :team :name :pred :hist :best-v :2nd-v :best-p :2nd-p :f-pred]
      league-summary)
    (println)
    (pp/print-table [:hor :das :las :ias :slr] [best-keys-freq sec-best-freq])))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;                  Main Engine                 ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn -main [& args]
  (let [raw-league-data (load-raw-data
                          (load-a-file (or (first args) "NED.edn")))
        team-data       (construct-teams raw-league-data)
        analysis        (map analyse team-data)
        predict-ranks   (predict-ranks analysis (:teams raw-league-data))]
    (print-summary analysis predict-ranks)))
