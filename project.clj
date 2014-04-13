(defproject clj-meetup "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [cascalog/cascalog-core "2.1.0"]
                 [cascalog/midje-cascalog "2.1.0"]
                 [cascalog/cascalog-checkpoint "2.1.0"]
                 [cascalog/cascalog-math "2.1.0"]
                 [incanter/incanter-core "1.5.4"]
                 [incanter/incanter-charts "1.5.4"]
                 [incanter/incanter-io "1.5.4"]
                 [incanter/incanter-pdf "1.5.4"]]
  :profiles {:dev {:dependencies [[org.apache.hadoop/hadoop-core "1.2.1"]]}}
  :main clj-meetup.core)
