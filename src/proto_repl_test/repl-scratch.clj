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
