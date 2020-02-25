(ns todo.core
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [marge.core :as marge])
  (:import (java.io File)))

(defmacro todo [& body])

(defn clojure-file? [^File file]
  (str/includes? (.getName file) ".clj"))

(defn get-top-level-forms [rdr]
  (map
    (comp read-string #(reduce str %))
    (filter #(not (= % [""]))
            (map vec (partition-by empty? (dedupe (reduce
                                                    conj []
                                                    (line-seq rdr))))))))

(defn reported? [form]
  (and (vector? (second form))
       (= :id (first (second form)))))

(defn todo? [form]
  (or (str/ends-with? (first form) "/todo")
      (= (str (first form)) "todo")))

(defn unreported-todo? [form]
  (and (todo? form)
       (not (reported? form))))

(defn gen-md [form]
  (let [len (count form)]
    (if (>= 1 len)
      nil
      (loop [marge-rep []
             idx 1]
        (if (= idx len)
          marge-rep
          (let [subform (nth form idx)]
            (cond (string? subform) (recur (conj marge-rep :normal (str subform "<br>" \newline))
                                           (inc idx))
                  (list? subform) (recur (conj marge-rep :code {:clojure (str subform)})
                                         (inc idx))
                  (and (vector? subform)
                       (not (= :id (first subform)))) (recur (conj marge-rep (first subform) (second subform))
                                                             (inc idx))
                  :else (recur marge-rep (inc idx)))))))))

(defn -main [& args]
  (let [project-dir (io/file "./")
        files (file-seq project-dir)
        clj-files (filter clojure-file? files)
        file (first clj-files)]
    (with-open [rdr (clojure.java.io/reader (.getAbsolutePath file))]
      (let [forms (get-top-level-forms rdr)]
        (doseq [unreported-form (filter unreported-todo? forms)]
          (spit "resources/test.md" (marge/markdown (gen-md unreported-form))))))))