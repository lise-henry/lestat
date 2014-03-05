(ns lestat.config)

(def file-name (atom ""))
(def targets (atom '(("Character1, Alias1, Alias2")
                     ("Character2")
                     ("Character3"))))

(def prefs (atom {:marker? false
                  :char-window 50000
                  :char-step 2000
                  :lines-window 200
                  :lines-step 10
                  :threshold-noun 25000
                  :regexp-chapter "=====+"}))                  