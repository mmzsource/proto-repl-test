(ns proto-repl-test.repl-scratch
  (:require [prc :as charts]
            [clj-http.client :as client]
            [clojure.pprint :as pprint]
            [clojure.java.io :as io]
            [clojure.edn :as edn]))

(def base-url "http://api.football-data.org/v1/")

(def competitions (client/get
                    (str base-url "competitions?season=2015")
                    {:as :json}))

(get-in (first (:body competitions)) [:_links :leagueTable :href])

(def competition-url-map
  (for [year [:2006 :2007 :2008 :2009 :2010 :2011 :2012 :2013 :2014 :2015 :2016 :2017]]
     {year {:url (str base-url "competitions?season=" (name year))}}))

(:2006 competition-url-map)

(map
  #(select-keys % [:id :caption :league :year])
  (:body competitions))

(defn filter-competition [competitions-year-info]
  (map
    #(select-keys % [:id :caption :league :year])
    competitions-year-info))

(def competitions-map
  (for [url competition-urls]
    (map #({filter-competition % (:body (client/get url {:as :json}))}))))

(def eng-2017 (client/get
                 "http://api.football-data.org/v1/competitions/426/leagueTable?season=2107"
                 {:as :json}))

(def nl-data
  (edn/read-string
    (slurp
      (io/resource "NL-eindstanden.edn"))))

(keys nl-data)

(:teams nl-data)

(:ranks nl-data)

(def ranks (get-in nl-data [:ranks :2016-2017]))

(zipmap ranks (range 1 40))

(conj [1 2] 3)

;; Map met als keys alle team in de NL competitie en als val een lege vector
(def team-positions
  (atom (zipmap (keys (:teams nl-data)) (repeatedly vector))))

(swap! team-positions update-in [:AJA] conj 1)

(def test-data
  {:teams {:AJA "Ajax" :FEY "Feyenoord" :PSV "PSV"}
   :ranks {:2016-2017 [:FEY :AJA :PSV]
           :2015-2016 [:PSV :AJA :FEY]}})

(def test-team-positions
  (atom (zipmap (keys (:teams test-data)) (repeatedly vector))))

(for [team (keys (:teams test-data))
      year [:2015-2016 :2016-2017]]
  (let [year-positions (zipmap (get-in test-data [:ranks year]) (range 1 50))]
    (swap! test-team-positions update-in [team] conj (team year-positions))))


(def team-positions
  (atom (zipmap (keys (:teams nl-data)) (repeatedly vector))))

(defn bla [competition-data year-keys]
  (for [team (keys (:teams competition-data))
        year year-keys]
    (let [year-positions (zipmap (get-in competition-data [:ranks year]) (range 1 50))]
      (swap! team-positions update-in [team] conj (team year-positions)))))

(bla nl-data (sort (keys (:ranks nl-data))))

(def ranks2 @team-positions)

(pprint/pprint ranks2)

(charts/scatter-chart "Position scatter" (:ranks2 nl-data))

;; 20170806
;; Start working with normalized dataset

(def nl-normalized-data
  (edn/read-string
    (slurp
      (io/resource "NL-eindstanden-normalized.edn"))))

(:rows nl-normalized-data)

(defn filter-by-index [coll idxs]
  (map (partial nth coll) idxs))

(filter-by-index ["A" "B" "C" "D"] [2 3])

(filter-by-index (first (:rows nl-normalized-data)) [2 3 4 5 6 7 8 9 10 11 12])

(map #(filter-by-index % [2 3 4 5 6 7 8 9 10 11 12]) (:rows nl-normalized-data))

(map #(prn %) [["1" "Z"] ["2"] ["A"] ["B"]])

(def row [:a 1 2 3 4])

(subvec row 1)

(defn transform [row]
  (hash-map (first row) (subvec row 2)))

(transform row)

(map transform (:rows nl-normalized-data))

(def plays [{:band "Burial",     :plays 979,  :loved 9}
            {:band "Eno",        :plays 2333, :loved 15}
            {:band "Bill Evans", :plays 979,  :loved 9}
            {:band "Magma",      :plays 2665, :loved 31}])

(map #(update-in (select-keys % [:band]) [:rate] {}) plays)

(def palindromes ["hannah" "kayak" "civic" "deified"])

(defn longest-palindrome [words]
  (->> words
    (filter #(= (seq %) (reverse %)))
    (map #(vector (count %) %))
    (sort-by first >)))

(take 10 (longest-palindrome palindromes))

(mapv + [1 2 3] [4 5 6])

(defn non-nil-weight-pos-vec [[w p]]
  (not (nil? p)))

(filter non-nil-weight-pos-vec (mapv vector [:a :b :c] [1 nil 3]))

(defn find-weights-for [weights-key weights]
  (let [haystack (:rows weights)
        needle   (first (filter #(= weights-key (first %)) haystack))]
     (subvec needle 2 12)))

(def sia-weights   (find-weights-for :sia raw-weights-data))
(def ia-weights    (find-weights-for :ia  raw-weights-data))
(def lin-weights   (find-weights-for :lin raw-weights-data))
(def da-weights    (find-weights-for :da  raw-weights-data))
(def sda-weights   (find-weights-for :sda raw-weights-data))
(def na-weights    (find-weights-for :na  raw-weights-data))

(prn (->> [0.500 0.550 0.587 0.611 0.623 0.623 0.611 0.587 0.550 0.500]
       (map #(- 1 %))))

(pprint/pprint
  (map reverse [[:da1  "decreasing ascend 1"  0.062 0.557 0.721 0.822 0.888 0.935 0.966 0.986 0.997 0.999]
                [:da2  "decreasing ascend 2"  0.007 0.396 0.584 0.708 0.798 0.865 0.915 0.953 0.981 0.998]
                [:da3  "decreasing ascend 3"  0.003 0.265 0.446 0.582 0.690 0.778 0.850 0.910 0.959 0.998]
                [:da4  "decreasing ascend 4"  0.002 0.173 0.322 0.454 0.570 0.674 0.768 0.853 0.930 0.998]
                [:la   "lineair ascend"       0.001 0.112 0.223 0.334 0.445 0.556 0.667 0.778 0.889 0.999]
                [:ia4  "increasing ascend 4"  0.002 0.070 0.147 0.232 0.326 0.430 0.546 0.678 0.827 0.998]
                [:ia3  "increasing ascend 3"  0.002 0.041 0.090 0.150 0.222 0.310 0.418 0.554 0.735 0.997]
                [:ia2  "increasing ascend 2"  0.002 0.019 0.047 0.085 0.135 0.202 0.292 0.416 0.604 0.993]
                [:ia1  "increasing ascend 1"  0.001 0.003 0.014 0.034 0.065 0.112 0.178 0.279 0.443 0.938]]))

(defn la2 [x] (+ 0.25 (* 0.5 x)))

(map la2 (range 0.001 1 0.111))

;; Finding the keys of the best 3 predictions:

(def m {:p1 5 :p2 7 :p3 9 :p4 2 :p5 8 :lp 6 :na "not applicable"})

(def fm (select-keys m [:p1 :p2 :p3 :p4 :p5]))

(def diff-fm (zipmap (keys fm) (map #(Math/abs (- (:lp m) %)) (vals fm))))

(keys (take 3 (sort-by val < diff-fm)))

(def test-map {:a :fiets :b :bel})
(defn work-with-test-map-entry [[k v]]
  (prn "key: " k " - val: " v))
(map work-with-test-map-entry test-map)
