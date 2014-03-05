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


(declare choose-file)
(declare choose-characters)

(defn menubar
  []
  "() -> Menubar
   Set actions and items for menubar and returns it"
  (let [open (sc/action :name "Load file"
                        :handler (fn [e]
                                   (sc/config! main-frame :content (choose-file))
                                   (sc/pack! main-frame)))
        edit (sc/action :name "Edit")
        about (sc/action :name "About")]
    (sc/menubar :items
                [(sc/menu :text "File" :items [open])
                 (sc/menu :text "Settings" :items [edit])
                             (sc/menu :text "Help" :items [about])])))

(defn listen-to-file-selection
  [button]
  "Button -> ()
   Add a listener whose action is to go back at beginning of program"
  (sc/listen button :action
             (fn [e]
               (sc/config! main-frame :content (choose-file))
               (sc/pack! main-frame))))

(defn listen-from-file-selection
  [button field next-step]
  "Add a listener whose action is to check file-name is ok and move next step"
  (println next-step)
  (sc/listen button :action
             (fn [e]
               (let [file-name (sc/config field :text)
                     f (java.io.File. file-name)]
                 (if (.canRead f)
                   (do
                     (swap! config/file-name (constantly file-name))
                     (sc/config! main-frame :content
                                 (next-step))
                       (sc/pack! main-frame))
                     (sc/alert e (str "Can't read file \"" 
                                      file-name
                                      "\"")))))))

(defn view-data
  [data prev-step]
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
                 (sc/config! main-frame :content (prev-step))
                 (sc/pack! main-frame)))
    (sc/vertical-panel :items [panel (XChartPanel. ^Chart chart)])))

(defn dialog-settings
  []
  "Create a panel allowing to set parameters for dialog repartition"
  (let [text (slurp @config/file-name)
        go-button (sc/button :text "View")
        back-button (sc/button :text "Back")
        bg-data (sc/button-group)
        bg-view (sc/button-group)
        items ["Data by..."
               (sc/radio :id :chapters :group bg-data :text "chapters" :selected? true)
               (sc/radio :id :window :group bg-data :text "sliding window")
               (sc/radio :id :absolute :group bg-view :text "absolute value" :selected? true)
               (sc/radio :id :percent :group bg-view :text "percent")
               (sc/horizontal-panel :items [back-button go-button])]
        panel (sc/vertical-panel :items items)]
    (listen-to-file-selection back-button)
    (sc/listen go-button :action
               (fn [e]
                 (let [chapters? (= (sc/config (sc/selection bg-data) :id) :chapters)
                       percent? (= (sc/config (sc/selection bg-view) :id) :percent)
                       data (if chapters? 
                              (analysis/dialog-data-by-chapters text)
                              (analysis/dialog-data-by-window text))
                       data (if percent?
                              (analysis/absolute->percent data)
                              data)]
                   (sc/config! main-frame :content (view-data data dialog-settings))
                   (sc/pack! main-frame))))
    panel))

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
        panel (sc/border-panel :vgap 5
                               :hgap 5
                               :border 5
                               :minimum-size [640 :by 480]
                               :center (sc/vertical-panel :border 5
                                                          :items [(sc/scrollable area)
                                                                  (sc/horizontal-panel :border 5
                                                                                       :items [find-button "Select only " spinner " more frequent"])])
                               :north (sc/horizontal-panel :border 5
                                                           :items [back-button 
                                                                   (sc/text :multi-line? true
                                                                            :editable? false 
                                                                            :focusable? false
                                                                            :text "Now, we need to know what words correspond to characters names.
You can either enter those manually, or try to use the auto-detect function.")])
                               :east (sc/vertical-panel :items items))]
    (listen-to-file-selection back-button)
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
                              (analysis/data-by-floating-window text))
                       data (if percent?
                              (analysis/absolute->percent data)
                              data)]
                   (sc/config! main-frame :content (view-data data choose-characters))
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
        character-button (sc/button :text "Character repartition")
        dialog-button (sc/button :text "Dialog/narration stats")
        panel (sc/border-panel :vgap 5
                               :hgap 5
                               :border 5
                               :minimum-size [640 :by 480]
                               :north txt
                               :west field
                               :east button
                               :south (sc/horizontal-panel :items [character-button dialog-button]))]
    (sc/listen button :action 
               (fn [e]
                 (let [f (sch/choose-file)]
                   (if f
                     (do (sc/text! field (str f))
                         (sc/pack! main-frame))
                     nil))))
    (listen-from-file-selection character-button field choose-characters)
    (listen-from-file-selection dialog-button field dialog-settings)
    panel))


(defn main-window
  []
  "Starting point of the gui"
  (sc/native!)
  (-> main-frame
      (sc/config! :content (choose-file))
      (sc/config! :menubar (menubar))
      sc/pack!
      sc/show!))