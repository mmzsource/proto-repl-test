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

(def pre-predictions (map team-averages (:rows raw-league-data)))

(defn find-best-predictor [team-predictions]
  (let [predictions        (select-keys (first pre-predictions) (keys weights-data))
        diff-with-last-pos (zipmap
                            (keys predictions)
                            (map #(Math/abs (- (:last-pos team-predictions) %))
                                 (vals predictions)))
        best-predictor      (key (first (sort-by val < diff-with-last-pos)))]
      {(:team team-predictions) best-predictor}))

(defn find-best-predictors [team-predictions-seq]
  (reduce merge (map find-best-predictor team-predictions-seq)))

(def best-predictors (find-best-predictors (map team-averages (:rows raw-league-data))))

(defn predict-new-position [[team best-weight-curve]]
  {team (calc-avg-pos (weight-position-seq (best-weight-curve weights-data) (rest (team league-data))))})

(defn predict-new-positions [best-predictors]
  (reduce merge (map predict-new-position best-predictors)))

(def predicted-positions
  (sort-by val < (select-keys
                   (predict-new-positions best-predictors)
                   [:ADO :AJA :AZA :EXC :FEY :GRO :HEA :HEE :NAC :PEC
                    :PSV :ROD :SPR :TWE :UTR :WIL :VIT :VVV])))

;; Best predicators for last season:
{:EXC :ia1,
 :RKC :ia1,
 :DOR :dd1,
 :CAM :ia1,
 :A29 :id1,
 :GRA :ia1,
 :JAJ :ia1,
 :HEL :ia1,
 :ALC :ia1,
 :UTR :ia1,
 :ROD :ia1,
 :NEC :ia1,
 :WIL :ia1,
 :AZA :ia1,
 :BOS :ia1,
 :HEA :ia1,
 :HEE :ia1,
 :PSV :ia1,
 :VIT :ia1,
 :GRO :ia1,
 :NAC :ia1,
 :TEL :ia1,
 :SIT :ia2,
 :GAE :ia1,
 :SPR :ia1,
 :OSS :ia1,
 :JPS :ia1,
 :ADO :ia1,
 :MVV :ia1,
 :TWE :ia1,
 :EHV :ia1,
 :PEC :ia1,
 :VVV :ia1,
 :EMM :ia1,
 :FEY :ia1,
 :VOL :ia1,
 :AJA :ia1}

;; Predicted positions for this season:

([:AJA 1.8132559264634733]
 [:FEY 2.238993710691824]
 [:PSV 2.333817126269956]
 [:AZA 5.447992259313013]
 [:UTR 6.062893081761006]
 [:VIT 6.188195452346395]
 [:GRO 7.781325592646348]
 [:TWE 8.129172714078374]
 [:HEE 8.903725205611996]
 [:HEA 10.147556845670053]
 [:ADO 11.087082728592163]
 [:PEC 11.53894533139816]
 [:WIL 14.313981615868407]
 [:EXC 15.295113691340106]
 [:ROD 16.468311562651184]
 [:SPR 19.660861151427188]
 [:NAC 19.797290759554908]
 [:VVV 20.084663763909045])
