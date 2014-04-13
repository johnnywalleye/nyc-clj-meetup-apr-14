(ns clj-meetup.demos-test
  (:use [clj-meetup.demos]
        [cascalog.api :only [hfs-textline]]
        [midje sweet cascalog])
  (:require [incanter.core :as i]))

(future-fact "word count should words from hfs-textline"
      (word-count :input-path) => (produces [["and" 1]
                                             ["more" 1]
                                             ["words" 2]])
      (provided (hfs-textline :input-path) =>
                ["words and more words"]))

(let [impression-data [["impression-1" {:color "red" :position 1}]
                       ["impression-2" {:color "blue" :position 3}]]
      click-data [["impression-1" "click-1"]]]
  (future-fact "impressions->clicks-inner should inner join impressions and clicks"
        (impressions->clicks-inner :impressions-path :clicks-path) =>
        (produces [["impression-1" {:color "red" :position 1} 1]])
        (provided (hfs-textline :impressions-path) => impression-data
                  (hfs-textline :clicks-path) => click-data))

  (future-fact "impressions->clicks-outer should inner join impressions and clicks"
               (impressions->clicks-outer :impressions-path :clicks-path) =>
               (produces [["impression-1" {:color "red" :position 1} 1]
                          ["impression-2" {:color "blue" :position 3} 0]])
               (provided (hfs-textline :impressions-path) => impression-data
                         (hfs-textline :clicks-path) => click-data)))

(future-fact "feature-variance calculates variances of a matrix of features"
      (feature-variance :features-path) =>
      (produces [[0 2/3 2/9]])
      (provided (hfs-textline :features-path) => [[[1 2 1]]
                                                  [[1 3 1]]
                                                  [[1 4 2]]]))

(fact "linear-regression calculates covariance vectors given a matrix of features"
      (linear-regression :y-value-with-features-path) =>
      (produces [[-2.5 1.0 -4.0 3.5]])
      (provided (hfs-textline :y-value-with-features-path) => [[[1 1.0 2.0 3.0]]
                                                  [[0 2.0 2.5 3.0]]
                                                  [[2 3.0 4.0 5.0]]
                                                  [[3 4.0 4.0 5.0]]
                                                  [[4 5.0 4.0 5.0]]
                                                  ]))
