(defproject lestat "0.1.0-SNAPSHOT"
  :description "A tiny statistical tool analysing the importance of characters in novels"
  :url "https://github.com/lady-segfault/lestat"
  :license {:name "GNU General Public License V3"
            :url "https://www.gnu.org/copyleft/gpl.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.xeiam.xchart/xchart "2.3.1"]]
  :source-paths ["src"]
  :main lestat.core)
