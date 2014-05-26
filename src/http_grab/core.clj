(ns http-grab.core
    (:require [clj-http.client :as client])
    ;(:require [clojurewerkz.urly.core :as urly])
    (:require [clojure.java.io :as io])
    (:require [clojure.core.async :as async])
    (:require [clojure.string :as string])
    (:gen-class)
)

(defn dir-from-path 
   "assuming final part of path is a file, return the stuff before"
   [path]
   (subs path 0 (.lastIndexOf path java.io.File/separator)))

(defn file-from-path 
   "assuming final part of path is a file, return the file"
   [path]
   (last (string/split path (re-pattern java.io.File/separator))))

(defn path-from-url
    "tranform a url into a local file path"
    [url-str]
    (let [u (client/parse-url url-str)]
    (let [path (-> (:server-name u) 
        (io/file (string/join (rest (:uri u))))
        (.getPath))]
    (if (= (:server-name u) path)
        (str path "/_index") 
        path))))

(defn delete-file-recursively [f]
  (let [f (io/file f)]
  (if (.isDirectory f)
      (doseq [child (.listFiles f)]
          (delete-file-recursively child)))
  (io/delete-file f)))

(defn write-file [path content]
  "makes path and writes file, returns file location if successful"
  (do
  (io/make-parents path) 
  (with-open [w (io/writer path :append false)]
      (.write w content)))
  (when (.exists (io/as-file path))
   path))

(defn save-url 
    "download url and store it, return path it was stored in"
    [url]
    (let [resp (client/get url)]
    (if (client/success? resp)
    (let [p (path-from-url url)]
    (if (write-file p (:body resp))
        p
        (throw (Throwable. (str "Unable to write file: " p)))))
    (throw (Throwable. (str "Unable to get a response: " url ))))))

(defn try-save-url
    [url]
    (try
    (println (str "Get: " url))
    ;(save-url url)
    (save-url url)
    ;(.start (Thread. (save-url url)))
    (catch Exception e (println (str "Error: " (.getMessage e))))))

(defn -main
    "read from stdin, pass to download method"
    []
    (let [urls (line-seq (java.io.BufferedReader. *in*))]
    (doseq [u urls] (async/go (try-save-url u)))))
    ;(pmap try-save-url urls)))
