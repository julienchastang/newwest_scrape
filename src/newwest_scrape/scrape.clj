(ns newwest_scrape.scrape
  (:require [net.cgrand.enlive-html :as html]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(defmacro slow
  [form]
  `(do (Thread/sleep 5000) ~form))

(def base-url "http://www.newwest.net/main/author_archive/jlowercase/")

(def base-urls  (map #(str base-url %) (range 0 700 10)))

(defn fetch-url
  [url]
  (html/html-resource (java.net.URL. url)))

(def links (apply concat (map #(slow (html/select (fetch-url %) [:a])) base-urls)))

(defn substring?
  [s str]
  (when (and s str)
    (.contains str s)))

(defn cleanup [s]
  (-> s
      (.replaceAll " " "_")
      (.replaceAll "[^a-zA-Z0-9_]" "")
      (.replaceAll "_+" "_")))

(def print-links
  (set (filter #(and (substring? "print" %)
                     (substring? "newwest" %))
               (map #(-> :attrs % :href) links))))

(defn my-subs [s b e]
  (if (>= (.length s) e)
    (subs s 0 e)
    s))

(defn download
  [uri dir]
  (try
    (let [article (html/html-resource (java.net.URL. uri))
          title (-> (html/select article
                                 [:title]) first :content first str/trim)
          file (my-subs (cleanup title) 0 40)]
      (with-open [in (io/input-stream uri)
                  out (io/output-stream (str dir file))]
        (io/copy in out)))
    (catch Exception e (println "Could not get " uri))))

(defn -main [& args]
  (map #(slow (download % "data/")) print-links))