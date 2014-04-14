(ns clj-meetup.screenshots
  (:use [cascalog.api]
        [cascalog.checkpoint])
  (:require [cascalog.logic.ops :as c]
            [cascalog.playground :as p]))


;-------------------------
; parallelagg vs. bufferfn
;-------------------------

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

;------------------------
; workflow
;------------------------
; Just example code to illustrate a workflow - queries not implemented

;(defn run-workflow [input-path-1 input-path-2 workflow-output-path]
;  (let [workflow-tmp-path "/tmp/generate-model-workflow"]
;    (with-job-conf { "mapred.reduce.tasks" 500
;                     "mapred.output.compress" "true"
;                     "mapred.output.compression.type" "BLOCK"}
;                   (workflow [workflow-tmp-path]
;                             step-1 ([:deps []
;                                          :tmp-dirs step-1-path]
;                                         (?- (hfs-seqfile step-1-path)
;                                             (query-1 input-path-1)))
;                             step-2  ([:deps []
;                                           :tmp-dirs step-2-path]
;                                          (?- (hfs-seqfile step-2-path)
;                                              (query-2 input-path-2)))
;                             step-3  ([:deps [step-1 step-2]
;                                           :tmp-dirs step-3-path]
;                                          (?- (hfs-seqfile step-3-path)
;                                              (query-3 step-1-path step-2-path)))
;                             step-4 ([:deps [step-3]]
;                                         (?- (make-output-tap workflow-output-path)
;                                             (query-4 step-3-path)))))))

