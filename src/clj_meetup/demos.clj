(ns clj-meetup.demos
  (:import [org.apache.commons.math.linear MatrixUtils OpenMapRealMatrix])
  (:use [cascalog.api])
  (:require [cascalog.logic.ops :as c]
            [cascalog.playground :as p]
            [incanter.core :as i]))

;---------------------------------
; Word Count
;---------------------------------
(defn word-count [input-path]
      (let [split (mapcatfn [text]
                            (.split text "\\s+"))]
        (<- [?word ?count]
              ((hfs-textline input-path) ?words)
              (split ?words :> ?word)
              (c/count :> ?count))))

;---------------------------------
; Joins
;---------------------------------
(defn impressions->clicks-inner [impressions-path clicks-path]
  (<- [?impression-id ?impression-characteristics ?number-of-clicks]
      ((hfs-textline impressions-path) ?impression-id ?impression-characteristics)
      ((hfs-textline clicks-path) ?impression-id)
      (c/count :> ?number-of-clicks)))

(defn impressions->clicks-outer [impressions-path clicks-path]
  (<- [?impression-id ?impression-characteristics ?number-of-clicks]
      ((hfs-textline impressions-path) ?impression-id ?impression-characteristics)
      ((hfs-textline clicks-path) ?impression-id !!click-id)
      (c/!count !!click-id :> ?number-of-clicks)))


;---------------------------------
; Feature Variance
;---------------------------------
(defn init-tuple [& tuple] [tuple])

(defn- square-tuple [tuple] (let [x (flatten tuple)] [(map * x x)]))

(defn- tuple-sum [x y] [(map + (flatten x) (flatten y))])

(defparallelagg square-sum :init-var #'square-tuple :combine-var #'tuple-sum)

(defparallelagg sum :init-var #'init-tuple :combine-var #'tuple-sum)

(defn- tuple-div [denominator numerators] [(vec (map #(/ % denominator) numerators))])

(defn- tuple-subtract [lhs rhs] [(vec (map - lhs rhs))])

(defn feature-variance [features-path]
  (<- [?variances]
      ((hfs-textline features-path) ?features)
      (c/count ?count)
      (sum ?features :> ?sums)
      (square-sum ?features :> ?square-sums)
      (tuple-div ?count ?sums :> ?means)
      (square-tuple ?means :> ?means-squared)
      (tuple-div ?count ?square-sums :> ?i-values)
      (tuple-subtract ?i-values ?means-squared :> ?variances)))


;---------------------------------
;Linear Regression
;---------------------------------

(defn compute-covariance-vectors [y-values-with-features]
  (let [x (i/matrix [(cons 1 (rest y-values-with-features))])
        y (i/matrix [[(first y-values-with-features)]])
        xt (i/trans x)
        xtx (i/mmult xt x)
        xty (i/mmult xt y)]
    [xtx xty]))

(defn- add-matrix-as-pairs [a b c d]
  [(i/plus a c) (i/plus b d)])

(defparallelagg covariance-vectors
                :init-var #'compute-covariance-vectors
                :combine-var #'add-matrix-as-pairs)

(defn get-covariance-vectors [features-with-y-values-path]
  (<- [?xtx ?xty]
      ((hfs-textline features-with-y-values-path) ?features-with-y-values)
      (covariance-vectors ?features-with-y-values :> ?xtx ?xty)))

(defn inverse-and-multiply [xtx xty]
  (vector (i/to-list (i/mmult (i/solve xtx) xty))))

(defn linear-regression [features-with-y-values-path]
  (<- [?regression-parameters]
      ((get-covariance-vectors features-with-y-values-path) :> ?xtx ?xty)
      (inverse-and-multiply ?xtx ?xty :> ?regression-parameters)))
