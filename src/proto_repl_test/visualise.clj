(ns proto-repl-test.visualise
  (:require [prc :as charts]
            [clojure.java.io :as io]
            [clojure.edn :as edn]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;     Visualise input data     ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(def raw-league-data
  (edn/read-string
    (slurp (io/resource "NL.edn"))))


(def raw-weights-data
  (edn/read-string
    (slurp (io/resource "weights.edn"))))


(defn transform-raw-data [raw-data]
  (->> (:rows raw-data)
       (map #(hash-map (first %) (subvec % 2)))
       (reduce merge)))


(def league-data (transform-raw-data raw-league-data))


(def weights-data (transform-raw-data raw-weights-data))


(charts/scatter-chart "League Data" league-data)


(charts/scatter-chart "Weights Data" weights-data)
