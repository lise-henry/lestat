(ns lestat.core
  (:gen-class)
  (:require [lestat.analysis :as analysis])
  (:import (javax.swing JFrame)
           (com.xeiam.xchart Chart SwingWrapper StyleManager StyleManager$LegendPosition)))

(defn -main
  "Main function"
  [& args]
  (let [text (slurp (if (zero? (count args))
                      "/tmp/test.txt"
                      (first args)))]
    (binding [analysis/targets (analysis/proper-nouns text 5)]
      (let [file-name (if (zero? (count args))
                        "/tmp/test.txt"
                        (first args))
            text (slurp file-name)
            data (analysis/data-by-floating-window text)
            
            chart (analysis/data->chart data)]
        (.setChartTitle ^Chart chart "Résultats")
        (.setLegendPosition ^StyleManager (.getStyleManager ^Chart chart) StyleManager$LegendPosition/InsideNE)
        (.displayChart (SwingWrapper. ^Chart chart))))))
  
  



