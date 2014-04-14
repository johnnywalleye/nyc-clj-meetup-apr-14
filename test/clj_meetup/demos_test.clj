(ns clj-meetup.demos-test
  (:use [clj-meetup.demos]
        [cascalog.api :only [hfs-textline]]
        [midje sweet cascalog])
  (:require [incanter.core :as i]))

(fact "word count should words from hfs-textline"
      (word-count :input-path) => (produces [["and" 1]
                                             ["more" 1]
                                             ["words" 2]])
      (provided (hfs-textline :input-path) =>
                ["words and more words"]))