(ns lestat.core
  (:gen-class)
  (:require [lestat.analysis :as analysis])
  (:import (javax.swing JFrame)
           (com.xeiam.xchart Chart SwingWrapper)))

(set! *warn-on-reflection* true)

(defn -main
  "Main function"
  [& args]
  (let [file-name "/tmp/test.txt"
        text (slurp file-name)
        data (analysis/data-by-chapters text)
        names '("Cookie" "Elvira")
        chart (analysis/data->chart data names)]
    (.displayChart (SwingWrapper. chart))))
    
  
