(ns http-grab.core-test
  (:require [clojure.test :refer :all]
            [http-grab.core :refer :all]))

(comment
(deftest main-test
  (testing "testing main returns nothing"
    (is (nil? (-main)))))) 

(def urls {
    :with-path "http://httpbin.org/get"
    :host-only "http://httpbin.org/"
    :with-query "http://httpbin.org/response-headers?Server=httpbin"
    :malformed "http:/httpbin.org/"
    :multi-path "http://httpbin.org/status/200"}) 
 
(deftest path-from-url-test
  (testing "testing path-from-url returns file path"
    (is (not (nil? (path-from-url (:with-path urls))))))
  (testing "testing path returns an index with only host"
    (is (.endsWith (path-from-url (:host-only urls)) "_index")))
  (testing "test if ignores query info"
    (is (.endsWith (path-from-url (:with-query urls)) "response-headers"))))

(defn apply-kv [m f]
  (into {} (for [[k v] m] [k (f v)]))) 
(def paths (apply-kv urls path-from-url))

(deftest dir-from-path-test
  (testing "testing dir is correct from path"
    (is (= "httpbin.org" (dir-from-path (:with-path paths))))
    (is (= "httpbin.org" (dir-from-path (:host-only paths))))
    (is (= "httpbin.org/status" (dir-from-path (:multi-path paths))))))


(deftest file-from-path-test
  (testing "testing file is correct from path"
    (is (= "get" (file-from-path (:with-path paths))))
    (is (= "_index" (file-from-path (:host-only paths))))
    (is (= "200" (file-from-path (:multi-path paths))))))

(defn delete-saved-files [f]
    ()
    (f)
    (delete-file-recursively "httpbin.org/"))

(use-fixtures :once delete-saved-files)

(deftest save-url-test
  (testing "testing process-url creates file"
    (is (= "httpbin.org/_index" (save-url (:host-only urls))))
    (is (= "httpbin.org/status/200" (save-url (:multi-path urls))))
    (is (= "httpbin.org/response-headers" (save-url (:with-query urls))))
    (is (thrown? org.apache.http.client.ClientProtocolException (save-url (:malformed urls))))
    (is (= "httpbin.org/get" (save-url (:with-path urls))))))
