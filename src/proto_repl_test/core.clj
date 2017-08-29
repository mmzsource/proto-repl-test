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


(defn transform-raw-data [raw-data]
  (->> (:rows raw-data)
       (map #(hash-map (first %) (subvec % 2)))
       (reduce merge)))

(def weights-data
  (transform-raw-data (load-raw-data (load-a-file "weights.edn"))))


(defn construct-teams [raw-league-data]
  (map
    (fn [[kw name & history]]
      (team/construct kw name (vec history)))
    (:rows raw-league-data)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Average position calculation with weight-curves ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn filtered-xy
  "Expects a vector of weights and a vector of positions,
   mapv's the items in vectors and removes the items where position data is nil."
  [weights position]
  (let [wp (mapv vector weights position)]
    (filter #(not (nil? (last %))) wp)))


(defn calc-avg-pos
  "Expects a vector of weights and a vector of positions,
   Removes nils and calculates average position based on that data:
   sum of (weight * year position) / sum of weights"
  [weights position]
  (let [weights-positions (filtered-xy weights position)]
    (/ (reduce + (map #(let [[w p] %] (* w p)) weights-positions))
       (reduce + (map first weights-positions)))))


(defn team-averages [weights-data team]
  "constructs a map of teamname, history, last-position and the calculated
   positions for every weight-curve."
  (let [team-history  (team/all-but-last-position team)]
    (merge
      {:team     (team/team-key team)
       :history  team-history
       :last-pos (team/last-position team)}
      (reduce merge
        (map #(hash-map
                %
                (calc-avg-pos (% weights-data) team-history))
           (keys weights-data))))))


(defn weight-curve-prediction [weights positions]
  (assert (= (count weights) (count positions)))
  (calc-avg-pos weights positions))

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


(defn linear-regression-prediction [positions]
  (assert (> (count positions) 1))
  (let [count           (inc (count positions))
        year-position   (filtered-xy (range 1 count) positions)
        xs              (reduce
                          (fn [v [x _]] (conj v x))
                          []
                          year-position)
        ys              (reduce
                          (fn [v [_ y]] (conj v y))
                          []
                          year-position)
        linreg-function (linreg-fn xs ys)]
    (double (linreg-function count))))


(linear-regression-prediction [1 2 nil 4 5])
(linear-regression-prediction [5 4 nil 2 1])


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;                  Predictions                 ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn predict [team]
  (let [all-but-last-position (team/all-but-last-position team)
        weight-curve-results
          (reduce
            (fn [m [calc-kw weights positions]]
              (assoc m calc-kw (weight-curve-prediction weights positions)))
            {}
            [[:da1 (:da1 weights-data) all-but-last-position]
             [:ia1 (:ia1 weights-data) all-but-last-position]
             [:hor (:hor weights-data) all-but-last-position]
             [:la1 (:la1 weights-data) all-but-last-position]])
        predictions (assoc
                      weight-curve-results
                      :slr
                      (linear-regression-prediction all-but-last-position))]
    predictions))

(defn find-best-predictor2 [predictions last-pos]
  (let [diff-with-last-pos (zipmap
                             (keys predictions)
                             (map #(Math/abs (- last-pos %))
                               (vals predictions)))]
    (key (first (sort-by val < diff-with-last-pos)))))

(def team [:PSV "PSV" [1 1 4 3 3 3 2 4 1 1 3]])

(defn report [team]
  (let [team-key           (team/team-key team)
        history            (team/history team)
        last-team-position (team/last-position team)
        predictions        (predict team)
        best-predictor     (find-best-predictor2 predictions last-team-position)
        prediction         (if (= best-predictor :slr)
                             (linear-regression-prediction history)
                             (weight-curve-prediction
                               (best-predictor weights-data)
                               (rest history)))]
    {:team team-key, :history history, :last-pos last-team-position,
     :predictions predictions, :best best-predictor, :prediction prediction}))

(report [:PSV "PSV" [1 1 4 3 3 3 2 4 1 1 3]])

(defn find-best-predictor [predictor-keys team-predictions]
  (let [predictions        (select-keys team-predictions predictor-keys)
        diff-with-last-pos (zipmap
                             (keys predictions)
                             (map #(Math/abs (- (:last-pos team-predictions) %))
                                  (vals predictions)))
        best-predictor       (key (first (sort-by val < diff-with-last-pos)))]
      {(:team team-predictions) best-predictor}))


(defn find-best-predictors [team-predictions weights-data]
  (reduce merge (map (partial find-best-predictor (keys weights-data)) team-predictions)))


(defn predict-new-position [team-data weights-data [team best-weight-curve]]
  {team (calc-avg-pos
          (best-weight-curve weights-data)
          (rest (team/history team-data)))})


(defn predict-new-positions [league-data weights-data best-predictors]
  (reduce merge (map (partial predict-new-position league-data weights-data) best-predictors)))


(defn new-setup []
  (let [raw-league-data     (load-raw-data
                              (load-a-file "NL.edn"))
        team-data           (construct-teams raw-league-data)
        raw-weights-data    (load-raw-data (load-a-file "weights.edn"))
        weights-data        (transform-raw-data raw-weights-data)
        team-predictions    (map (partial team-averages weights-data)
                                 team-data)
        best-predictors     (find-best-predictors team-predictions weights-data)
        predicted-positions (sort-by val <
                              (select-keys
                                (predict-new-positions
                                  team-data weights-data best-predictors)
                                (:teams raw-league-data)))]
    (pp/pprint (select-keys best-predictors (keys predicted-positions)))
    (pp/pprint predicted-positions)))

(team-averages )


(defn -main [& args]
  (let [raw-league-data     (load-raw-data
                              (load-a-file (or (first args) "NL.edn")))
        team-data           (construct-teams raw-league-data)
        raw-weights-data    (load-raw-data (load-a-file "weights.edn"))
        weights-data        (transform-raw-data raw-weights-data)
        team-predictions    (map (partial team-averages weights-data)
                                 team-data)
        best-predictors     (find-best-predictors team-predictions weights-data)
        predicted-positions (sort-by val <
                              (select-keys
                                (predict-new-positions
                                  team-data weights-data best-predictors)
                                (:teams raw-league-data)))]
    (pp/pprint (select-keys best-predictors (keys predicted-positions)))
    (pp/pprint predicted-positions)))
