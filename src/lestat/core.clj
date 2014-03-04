(ns lestat.core
  (:gen-class)
  (:require [lestat.analysis :as analysis])
  (:import (javax.swing JFrame)
           (com.xeiam.xchart Chart SwingWrapper StyleManager StyleManager$LegendPosition)))

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
    (.setLegendPosition ^StyleManager (.getStyleManager ^Chart chart) StyleManager$LegendPosition/InsideNE)
;;    (.setPlotTicksMarksVisible (.getStyleManager chart) false)
    (.displayChart (SwingWrapper. ^Chart chart))))
    
  
