(ns proto-repl-test.core
  (:require [prc :as charts]
            [clojure.java.io :as io]
            [clojure.edn :as edn]))

(def nl-league-data
  (edn/read-string
    (slurp
      (io/resource "NL-eindstanden-normalized.edn"))))

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
  [league-data]
  (->> (:rows league-data)
       (map transform-row)
       (reduce merge)
       (charts/scatter-chart "Position scatter")))

(show-scatter-chart nl-league-data)
