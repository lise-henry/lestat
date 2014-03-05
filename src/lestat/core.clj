(ns lestat.core
  (:gen-class)
  (:require [lestat.gui :as gui]))

(defn -main
  "Main function"
  [& args]
  (gui/main-window))  
  



