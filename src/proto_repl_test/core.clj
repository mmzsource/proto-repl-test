(ns proto-repl-test.core
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.pprint :as pp]))

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
  "Expects a vector of weights and a vector of positions,
   Removes nils and calculates average position based on that data:
   sum of (weight * year position) / sum of weights"
  [weights position]
  (let [weights-positions (weight-position-seq weights position)]
    (/ (reduce + (map #(let [[w p] %] (* w p)) weights-positions))
       (reduce + (map first weights-positions)))))


(defn team-averages [weights-data team-data]
  "Expects raw team-data from in the same format as in the edn file
   and constructs a map of teamname, history, last-position and the calculated
   positions for every weight-curve."
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
                (calc-avg-pos (% weights-data) team-history))
           (keys weights-data))))))


(defn find-best-predictor [weights-data team-predictions]
  (let [predictions        (select-keys team-predictions (keys weights-data))
        diff-with-last-pos (zipmap
                             (keys predictions)
                             (map #(Math/abs (- (:last-pos team-predictions) %))
                                  (vals predictions)))
        best-predictor       (key (first (sort-by val < diff-with-last-pos)))]
      {(:team team-predictions) best-predictor}))


(defn find-best-predictors [team-predictions weights-data]
  (reduce merge (map (partial find-best-predictor weights-data) team-predictions)))


(defn predict-new-position [league-data weights-data [team best-weight-curve]]
  {team (calc-avg-pos
          (best-weight-curve weights-data)
          (rest (team league-data)))})


(defn predict-new-positions [league-data weights-data best-predictors]
  (reduce merge (map (partial predict-new-position league-data weights-data) best-predictors)))


(defn -main [& args]
  (let [raw-league-data     (load-raw-data
                              (load-a-file (or (first args) "NL.edn")))
        league-data         (transform-raw-data raw-league-data)
        raw-weights-data    (load-raw-data (load-a-file "weights.edn"))
        weights-data        (transform-raw-data raw-weights-data)
        team-predictions    (map (partial team-averages weights-data)
                                 (:rows raw-league-data))
        best-predictors     (find-best-predictors team-predictions weights-data)
        predicted-positions (sort-by val <
                              (select-keys
                                (predict-new-positions
                                  league-data weights-data best-predictors)
                                (:teams raw-league-data)))]
    (pp/pprint (select-keys best-predictors (keys predicted-positions)))
    (pp/pprint predicted-positions)))
