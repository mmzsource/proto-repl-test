(ns soc-pred-17.team)

(defn construct [team-key team-name team-history]

  (assert (keyword? team-key))
  (assert (string?  team-name))
  (assert (vector?  team-history))
  (assert (> (count team-history) 1))
  (assert (every? #(or (number? %) (nil? %)) team-history))

  (vector team-key team-name team-history))

(defn- team-keyword [[team-key _ _]]
  team-key)

(defn- team-name-string [[_ team-name _]]
  team-name)

(defn- team-history [[_ _ team-history]]
  team-history)

(defn- team-last-position [[_ _ team-history]]
  (last team-history))

(defn- team-all-but-last-position [[_ _ team-history]]
  (subvec team-history 0 (- (count team-history) 1)))

(defn team-key [team]
  (team-keyword team))

(defn team-name [team]
  (team-name-string team))

(defn history [team]
  (team-history team))

(defn last-position [team]
  (team-last-position team))

(defn all-but-last-position [team]
  (team-all-but-last-position team))
