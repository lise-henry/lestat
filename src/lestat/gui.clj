(ns lestat.gui
  (:require [lestat.config :as config]
            [lestat.analysis :as analysis]
            [seesaw.chooser :as sch]
            [seesaw.core :as sc]
            [seesaw.font :as sf])
  (:import (com.xeiam.xchart Chart StyleManager$LegendPosition XChartPanel StyleManager)))

(def menubar 
  (sc/menubar :items
              [(sc/menu :text "File" :items [])
               (sc/menu :text "View" :items [])
               (sc/menu :text "About" :items [])]))
               

(def main-frame
  (sc/frame :title "Lestat"
            :size [640 :by 480]
;            :menubar menubar
            :on-close :dispose))

(declare choose-file)
(declare choose-characters)

(defn view-data
  [data]
  "Create a panel with a chart to view data"
  (let [chart (analysis/data->chart data)
        back-button (sc/button :text "Back")
        stats (str "File: "
                   @config/file-name
                   " ("
                   (count (slurp @config/file-name))
                   "cars), analysis on "
                   (count @config/targets)
                   " named characters")
        panel (sc/horizontal-panel :items [back-button stats])]
    (.setChartTitle ^Chart chart "Character 'popularity'")
    (.setLegendPosition ^StyleManager (.getStyleManager ^Chart chart) StyleManager$LegendPosition/InsideSW)
    (sc/listen back-button :action 
               (fn [e]
                 (sc/config! main-frame :content (choose-characters))
                 (sc/pack! main-frame)))
    (sc/vertical-panel :items [panel (XChartPanel. ^Chart chart)])))

(defn choose-characters
  []
  "Create a panel allowing to choose characters from a file"
  (let [text (slurp @config/file-name)
        area (sc/text :multi-line? true
                        :text (analysis/characters->string @config/targets))
        back-button (sc/button :text "Back")
        find-button (sc/button :text "Auto-detect characters names")
        go-button (sc/button :text "View")
        bg-data (sc/button-group)
        bg-view (sc/button-group)
        spinner (sc/spinner :model 
                            (sc/spinner-model 10 :from 1 :to 30 :by 1))
        items ["Data by..."
               (sc/radio :id :chapters :group bg-data :text "chapters" :selected? true)
               (sc/radio :id :window :group bg-data :text "sliding window")
               (sc/radio :id :absolute :group bg-view :text "absolute value" :selected? true)
               (sc/radio :id :percent :group bg-view :text "percent")
               go-button]  
        panel (sc/border-panel :minimum-size [640 :by 480]
                               :center (sc/vertical-panel :items [(sc/scrollable area)
                                                                  (sc/horizontal-panel :items [find-button "Select only " spinner " more frequent"])])
                               :north (sc/horizontal-panel :items [back-button 
                                                                   (sc/text :multi-line? true
                                                                            :editable? false 
                                                                            :focusable? false
                                                                            :text "Now, we need to know what words correspond to characters names.
You can either enter those manually, or try to use the auto-detect function.")])
                               :east (sc/vertical-panel :items items))]
    (sc/listen back-button :action
               (fn [e]
                 (sc/config! main-frame :content (choose-file))
                 (sc/pack! main-frame)))
    (sc/listen find-button :action
               (fn [e]
                 (let [c (analysis/proper-nouns text (sc/selection spinner))]
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
                              (analysis/data-by-floating-window text))]
                   (sc/config! main-frame :content (view-data data))
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