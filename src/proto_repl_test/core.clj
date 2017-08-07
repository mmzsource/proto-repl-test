(ns proto-repl-test.core
  (:require [prc :as charts]
            [clojure.java.io :as io]
            [clojure.edn :as edn]))

(def nl-league-data
  (edn/read-string
    (slurp (io/resource "NL-eindstanden-normalized.edn"))))

(def nl-weights
  (edn/read-string
    (slurp (io/resource "weights.edn"))))

(defn transform-row
  "Expects a vector with this structure:
   [:team-key 'team-description' pos-0607 pos-0708 ... pos-last-year]
   and returns a map with a structure fit for a scatterplot:
   {:team-key [pos-0607 pos-0708 ... pos-last-year]}"
  [row]
  (hash-map (first row) (subvec row 2)))

(defn show-scatter-chart
  "Transforms the league-data into this format:
   {:AJA [1 2 1 1 2 1 2 2]
    :FEY [2 3 3 3 3 2 3 1]
    :PSV [3 1 2 2 1 3 1 3]
    ... etc}
   and shows it in a scatter-chart"
  [scatter-data plot-name]
  (->> (:rows scatter-data)
       (map transform-row)
       (reduce merge)
       (charts/scatter-chart plot-name)))

(show-scatter-chart nl-league-data "Position Scatter")

(show-scatter-chart nl-weights "Weight Scatter")

(defn weight-position-map
  "Expects a vector of weights and a vector of positions,
   zipmaps the two and removes the items where position data is nil."
  [weights position]
  (let [wp (zipmap weights position)]
    (filter #(not (nil? (val %))) wp)))

(defn calc-avg-pos
  "Expects a map with weights as keys and positions as value.
   Calculates average position based on that data:
   sum of (weight * year position) / sum of weights"
  [weight-position-map]
  (/ (reduce + (map #(let [[k v] %] (* k v)) weight-position-map))
     (reduce + (keys weight-position-map))))

(defn find-weights-for [weights-key weights]
  (let [haystack (:rows weights)
        needle   (first (filter #(= weights-key (first %)) haystack))]
     (subvec needle 2 12)))

(def sia-weights   (find-weights-for :sia nl-weights))
(def ia-weights    (find-weights-for :ia  nl-weights))
(def lin-weights   (find-weights-for :lin nl-weights))
(def da-weights    (find-weights-for :da  nl-weights))
(def sda-weights   (find-weights-for :sda nl-weights))
(def na-weights    (find-weights-for :na  nl-weights))

(defn team-averages [team-data]
  (println team-data)
  (let [team-keyword  (first team-data)
        team-history  (subvec team-data 2 12)
        last-team-pos (last team-data)]
    (conj []
      team-keyword
      team-history
      last-team-pos
      (calc-avg-pos (weight-position-map sia-weights team-history))
      (calc-avg-pos (weight-position-map ia-weights  team-history))
      (calc-avg-pos (weight-position-map lin-weights team-history))
      (calc-avg-pos (weight-position-map da-weights  team-history))
      (calc-avg-pos (weight-position-map sda-weights team-history))
      (calc-avg-pos (weight-position-map na-weights  team-history)))))

(map team-averages (:rows nl-league-data))
