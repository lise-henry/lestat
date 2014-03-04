(ns lestat.analysis
  (:import (com.xeiam.xchart Chart Series SeriesMarker)))

;; default parameters
(def regexp-chapter-text #"Chapitre \d")
(def regexp-chapter-markdown #"=====+")
(def regexp-chapter regexp-chapter-markdown)
(def regexp-word #"[\p{IsAlphabetic}|-]+")
(def regexp-name #"\p{IsAlphabetic} (\p{Lu}[\p{IsAlphabetic}|-]*)")

;; display preferences 
;; todo: add gui
(def prefs-marker? false) ;; whether there are markers or not
(def prefs-floating-window 50000) ;; number of words to do stats
(def prefs-floating-step 1000) ;; step between each computation
(def prefs-threshold-noun 25000) ;; max period of occurrence of a proper noun

;; todo: remove following and add gui
(def file-name "/tmp/test.txt")
(def ^:dynamic targets '(("Razor" "Raz")
               ("Crow" "Kro")
               ("Cassandra" "Casse")
               ("Karima" "K")
               ("Cookie" "Cook")
               ("Betty" "Bête")
               ("Morgue")
               ("Shade")
               ("Elvira")))

(defn split-chapters 
  [text]
  "String -> List of Strings
   Split a string (containing whole text) into strings corresponding
   to each chapter"
  (remove empty? (clojure.string/split text regexp-chapter)))

(defn character-pattern
  [character]
  "List of Strings -> Regexp pattern
   Returns the regexp pattern allowing to find a character"
  (re-pattern (str ""
                   (clojure.string/join "|" character)
                   "")))

(defn find-pos
  [character text]
  "List of Strings, String -> List of positions
   Gives all position of mentions of a character"
  (let [pattern (character-pattern character)]
    (loop [matcher (re-matcher pattern text)
           result []]
      (if (.find matcher)
        (recur matcher
               (conj result (.start matcher)))
        result))))

(defn find-pos-all-characters
  [text]
  "String -> hashmap
  Apply find-pos on all characters, and returns a hashmap with results"
  (into {}
        (for [character targets]
          {(first character) (find-pos character text)})))


(defn data-from-positions
  [positions]
  "[Hashmap -> Data]
   Transform a map associating characters and positions
   to a map associating characters and occurrences"
  (into {}
        (map (fn [x]
               {(first x)
                (count (second x))})
             positions)))

(defn data-minimal
  [text]
  "String -> hasmap
   Return minimal global data for all text"
  (-> text
      find-pos-all-characters
      data-from-positions))

(defn data-by-floating-window
  [text]
  "String -> Data
   Use find-pos-all-characters to compute set of data every 
   prefs-floating-steps"
  (let [n prefs-floating-window
        step prefs-floating-step
        tmp-data (find-pos-all-characters text)
        end (- (count text) n)]
    (for [i (range 0 end step)]
      (into {}
            (map (fn [x]
                   {(first x)
                    (count (filter #(<= i % (+ i n))
                                   (second x)))})
                 tmp-data)))))

(defn data-by-chapters
  [text]
  "String -> Data
   Give statistics for all chapters"
  (->> text
       split-chapters
       (map data-minimal)))
    
(defn data->chart
  "Data, List of Strings? -> Chart
   Converts data to a chart which can be displayed. Only data related
   to characters in names are selected (all by default)."
  ([data]
     (data->chart data (map first targets)))
  ([data names]
     (let [chart (Chart. 500 300)]
       (doseq [n names]
         (let [series (->> data
                           (map #(% n))
                           (map double)
                           (.addSeries chart n nil))]
           (if-not prefs-marker?
             (.setMarker ^Series series SeriesMarker/NONE))))
       chart)))

(defn proper-nouns
  [text]
  "String -> List of Strings
   Try to identify proper nouns of a text."
  (let [candidates (map second (re-seq regexp-name text))
        threshold (/ (count text) prefs-threshold-noun)]
    (->> candidates
        frequencies
        (filter #(> (val %) threshold))
        (map #(list (first %))))))
