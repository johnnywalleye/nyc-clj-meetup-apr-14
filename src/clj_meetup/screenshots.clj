(ns clj-meetup.screenshots
  (:use [cascalog.api])
  (:require [cascalog.logic.ops :as c]
            [cascalog.playground :as p]))


;------------------------
; word count
;------------------------

(defn count-words []
  (let [split (mapcatfn [text]
                        (.split text "\\s+"))]
    (?<- (stdout) [?word ?count]
         (p/sentence :> ?sentence)
         (split ?sentence :> ?word)
         (c/count :> ?count))))

;------------------------
; parallelagg
;------------------------

(def numbers [1 2 3 5 8 11])

(defn get-sum []
  (let [sum (bufferfn [x]
                      [(reduce + (map first x))])]
    (?<- (stdout)
         [?sum]
         (numbers ?numbers)
         (sum ?numbers :> ?sum))))


(defparallelagg parallel-sum
                :init-var #'identity
                :combine-var #'+)

(defn get-sum-parallel []
  (?<- (stdout)
       ;(let [parallel-sum (parallelagg +)]
       [?sum]
         (numbers ?numbers)
         (parallel-sum ?numbers :> ?sum)))

