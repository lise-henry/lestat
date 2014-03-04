(ns lestat.analysis
  (:import (com.xeiam.xchart Chart)))

;; default parameters
(def regexp-chapter-text #"Chapitre \d")
(def regexp-chapter-markdown #"=====+")
(def regexp-chapter regexp-chapter-markdown)
(def regexp-word #"[\p{IsAlphabetic}|-]+")

;; todo: remove following and add gui
(def file-name "/tmp/test.txt")
(def targets '(("Razor" "Raz")
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

(defn split-words
  [text]
  "String->List of Strings
   Split a string into strings corresponding to words."
  (re-seq regexp-word text))

(defn character-pattern
  [character]
  "List of Strings -> Regexp pattern
   Returns the regexp pattern allowing to find a character"
  (re-pattern (str ""
                   (clojure.string/join "|" character)
                   "")))

(defn count-character
  [words character]
  "List of words, List of aliases -> hashmap
   Return a (single-element) hashmap giving the occurrences of 
   a given character's name(s)"
  {(first character) (count (filter (set character) words))})

(defn count-all-characters
  [words]
  "List of words -> hashmap
   Return a hasmap giving the occurences for each characters"
  (into {}
        (map #(count-character words %) targets)))

(defn data-by-chapters
  [text]
  "String -> Data
   Give statistics for all chapters"
  (map count-all-characters
       (map split-words
            (split-chapters text))))

(defn data-by-words
  [text n]
  "String, Int -> Data
   Give statistics for each chunk of n words"
  (map count-all-characters
       (partition n (split-words text))))

(defn data->chart
  "Data, List of Strings? -> Chart
   Converts data to a chart which can be displayed. Only data related
   to characters in names are selected (all by default)."
  ([data]
     (data->chart data (map first targets)))
  ([data names]
     (let [chart (Chart. 800 600)
        xdata (map double (range (count data)))]
       (doseq [n names]
         (->> data
              (map #(% n))
              (map double)
              (.addSeries chart n xdata)))
       chart)))

(defn data-minimal
  [text]
  "String -> hasmap
   Return minimal global data for all text"
  (count-all-characters (split-words text)))