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
; Generate Model Data
;---------------------------------
(def ad-copy [["impression-1" "buy this"]
           ["impression-2" "great deal"]
           ["impression-3" "cheap sale"]
           ["impression-4" "cheap sale"]])

(def clicks [["click-1" "impression-2" 0]
             ["click-2" "impression-3" 100]
             ["click-3" "impression-4" 0]])

(defn generate-model-data []
  (let [get-word-vector (mapfn [attributes]
                     (when attributes
                       [(vec (.split attributes "\\s+"))]))
        to-binary (mapfn [purchase-amount]
                         (if (and purchase-amount (> purchase-amount 0)) 1 -1))]
    (?<- (stdout) [?impression-id ?word-split ?did-purchase]
         (ad-copy ?impression-id ?ad-copy)
         (clicks !!click-id ?impression-id !!purchase-amount)
         (get-word-vector ?ad-copy :> ?word-split)
         (c/sum !!purchase-amount :> ?purchase-amount-sum)
         (to-binary ?purchase-amount-sum :> ?did-purchase))))


;---------------------------------
; Feature Variance
;---------------------------------
(def feature-variance-inputs [[[1 2 1]]
                              [[3 3 1]]
                              [[1 4 2]]])

; E(X^2) - E(X)^2 for each feature in our tuple

(defn init-tuple [& tuple] [tuple])

(defn- square-tuple [tuple] (let [x (flatten tuple)] [(map * x x)]))

(defn- tuple-sum [x y] [(map + (flatten x) (flatten y))])

(defparallelagg square-sum :init-var #'square-tuple :combine-var #'tuple-sum)

(defparallelagg sum :init-var #'init-tuple :combine-var #'tuple-sum)

(defn- tuple-div [denominator numerators] [(vec (map #(/ % denominator) numerators))])

(defn- tuple-subtract [lhs rhs] [(vec (map - lhs rhs))])

(defn feature-variance []
  (?<- (stdout) [?variances]
      (feature-variance-inputs ?features)
      (sum ?features :> ?sums)
      (square-sum ?features :> ?square-sums)
      (c/count ?count)
      (tuple-div ?count ?sums :> ?means)
      (square-tuple ?means :> ?means-squared)
      (tuple-div ?count ?square-sums :> ?i-values)
      (tuple-subtract ?i-values ?means-squared :> ?variances)))


;---------------------------------
;Linear Regression
;---------------------------------
(def linear-regression-inputs [[[1 1.0 2.0 3.0]]
                               [[0 2.0 2.5 3.0]]
                               [[2 3.0 4.0 5.0]]
                               [[3 4.0 4.0 5.0]]
                               [[4 5.0 4.0 5.0]]])

; X: feature matrix
; y: vector of outcomes
; Linear Regression: (X^T * X)^-1 * (X^T * y)

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

(defn get-covariance-vectors []
  (<- [?xtx ?xty]
      (linear-regression-inputs ?features-with-y-values)
      (covariance-vectors ?features-with-y-values :> ?xtx ?xty)))

(defn inverse-and-multiply [xtx xty]
  (i/mmult (i/solve xtx) xty))

(defn linear-regression []
  (?<- (stdout) [?regression-parameters]
      ((get-covariance-vectors) :> ?xtx ?xty)
      (inverse-and-multiply ?xtx ?xty :> ?regression-parameters)))
