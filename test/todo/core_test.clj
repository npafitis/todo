(ns todo.core-test
  (:require [clojure.test :refer :all]
            [todo.core :refer :all]))

(todo
  [:h1 "Issue"]
  "Needs Some fixing"
  "Hello World"
  (defn fixable-fn [in]
    (prn in)))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 1))))
