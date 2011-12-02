(ns newwest_scrape.core
  (:require  [net.cgrand.enlive-html :as html]
             [clojure.java.io :as io]))

(def base-url "http://www.newwest.net/main/author_archive/jlowercase/10/")

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(def links (html/select (fetch-url base-url) [:a]))

(defn substrin [s str]
  (when (and s str)
    (.contains str s)))

(def print-links (filter #(substring? "print" %)
                         (map #(-> :attrs % :href) links)))

(defn download  [uri file]
  (with-open [in (io/input-stream uri)
              out (io/output-stream file)]
        (io/copy in out)))
