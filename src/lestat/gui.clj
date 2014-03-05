(ns lestat.gui
  (:require [lestat.config :as config]
            [lestat.analysis :as analysis]
            [seesaw.chooser :as sch]
            [seesaw.core :as sc]
            [seesaw.font :as sf])
  (:import (com.xeiam.xchart Chart StyleManager$LegendPosition XChartPanel StyleManager)))

(def main-frame
  (sc/frame :title "Lestat"
            :size [640 :by 480]
            :on-close :dispose))

(defn choose-characters
  []
  "Create a panel allowing to choose characters from a file"
  (let [text (slurp @config/file-name)
        area (sc/text :multi-line? true
                        :text (analysis/characters->string @config/targets))
        find-button (sc/button :text "Auto-detect characters names")
        go-button (sc/button :text "View")
        bg-data (sc/button-group)
        bg-view (sc/button-group)
        items ["Data by..."
               (sc/radio :id :chapters :group bg-data :text "chapters" :selected? true)
               (sc/radio :id :window :group bg-data :text "sliding window")
               (sc/radio :id :absolute :group bg-view :text "absolute value" :selected? true)
               (sc/radio :id :percent :group bg-view :text "percent")
               go-button]  
        panel (sc/border-panel :minimum-size [640 :by 480]
                               :south find-button
                               :center (sc/scrollable area)
                               :north "Now, we need to know what words correspond to characters names.
You can either enter those manually, or try to use the auto-detect function."
                               :east (sc/vertical-panel :items items))]
    (sc/listen find-button :action
               (fn [e]
                 (let [c (analysis/proper-nouns text)]
                   (swap! config/targets (constantly c))
                   (sc/text! area 
                             (analysis/characters->string c)))))
    (sc/listen go-button :action
               (fn [e]
                 (swap! config/targets (constantly (analysis/string->characters (sc/text area))))
                 (let [chapters? (= (sc/config (sc/selection bg-data) :id) :chapters)
                       percent? (= (sc/config (sc/selection bg-view) :id) :percent)
                       data (if chapters? 
                              (analysis/data-by-chapters text)
                              (analysis/data-by-floating-window text))
                       chart (analysis/data->chart data)]
                   (.setChartTitle ^Chart chart "Stats")
                   (.setLegendPosition ^StyleManager (.getStyleManager ^Chart chart) StyleManager$LegendPosition/InsideSW)
                   (sc/config! main-frame :content (XChartPanel. ^Chart chart))
                   (sc/pack! main-frame))))
    panel))
        
    

(defn choose-file
  []
  "Create a panel allowing to choose file"
  (let [txt (sc/label :text
                      "Please choose a text file containing the text to analyse"
                      :font (sf/font :style #{:bold}))
        button (sc/button :text "Choose file")
        field (sc/text (if (empty? @config/file-name)
                         "No file selected"
                         @config/file-name))
        ok-button (sc/button :text "OK")
        panel (sc/border-panel :minimum-size [640 :by 480]
                               :north txt
                               :west field
                               :east button
                               :south ok-button)]
    (sc/listen button :action 
               (fn [e]
                 (let [f (sch/choose-file)]
                   (if f
                     (do (sc/text! field (str f))
                         (sc/pack! main-frame))
                     nil))))
    (sc/listen ok-button :action
               (fn [e]
                 (let [file-name (sc/config field :text)
                       f (java.io.File. file-name)]
                   (if (.canRead f)
                     (do
                       (swap! config/file-name (constantly file-name))
                       (sc/config! main-frame :content
                                   (choose-characters))
                       (sc/pack! main-frame))
                     (sc/alert e (str "Can't read file \"" 
                                      file-name
                                      "\""))))))
  panel))


(defn main-window
  []
  "Starting point of the gui"
  (sc/native!)
  (-> main-frame
      (sc/config! :content (choose-file))
      sc/pack!
      sc/show!))