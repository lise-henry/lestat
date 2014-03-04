(ns lestat.core
  (:gen-class)
  (:require [lestat.analysis :as analysis])
  (:import (javax.swing JFrame)
           (com.xeiam.xchart Chart SwingWrapper StyleManager StyleManager$LegendPosition)))

(set! *warn-on-reflection* true)

(defn -main
  "Main function"
  [& args]
  (let [file-name "/tmp/test.txt"
        text (slurp file-name)
        data (analysis/data-by-words text)
        names '("Cookie" "Elvira")
        chart (analysis/data->chart data)]
;;    (doto chart
;;      (.setChartTitle "Résultats"))
    (.setLegendPosition (.getStyleManager chart) StyleManager$LegendPosition/InsideNE)
;;    (.setPlotTicksMarksVisible (.getStyleManager chart) false)
    (.displayChart (SwingWrapper. chart))))
    
  
