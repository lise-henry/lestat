(ns lestat.core
  (:gen-class)
  (:require [lestat.analysis :as analysis]
            [lestat.gui :as gui])
  (:import (javax.swing JFrame)
           (com.xeiam.xchart Chart SwingWrapper StyleManager StyleManager$LegendPosition XChartPanel))
  (:use seesaw.core))

(defn -main
  "Main function"
  [& args]
;          data (analysis/data-by-floating-window text)
;          chart (analysis/data->chart data)]
;      (.setChartTitle ^Chart chart "Résultats")
;      (.setLegendPosition ^StyleManager (.getStyleManager ^Chart chart) StyleManager$LegendPosition/InsideSW)
  (gui/main-window))
; (XChartPanel. ^Chart chart)))))))


;; o         (-> (frame :title "Lestat"
;;                     :on-close :exit
;;                     :content (XChartPanel. ^Chart chart))
;;              pack!
;;              show!))))))
  
  



