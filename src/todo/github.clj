(ns todo.github
  (:require [clj-http.client :as client]
            [cheshire.core :as cheshire]))

(def base-url "https://api.github.com")

(defn read-todo-config []
  (read-string (slurp "todo.edn")))

(defn get-issues []
  (let [todo-config (read-todo-config)
        ;token (:personal-token todo-config)
        repo (:repo todo-config)]
    (cheshire/parse-string (:body (client/get
                                    (str base-url
                                         "/repos/"
                                         repo
                                         "/issues"))) true)))

(defn get-last-issue-id []
  (reduce max (map :number (get-issues))))

(defn post-issue [body]
  (let [todo-config (read-todo-config)
        token (:personal-token todo-config)
        user (:user todo-config)
        repo (:repo todo-config)
        issue-id (inc (get-last-issue-id))]
    (client/post (str base-url "/repos/" repo "/issues")
                 {:basic-auth   [user token]
                  :content-type :json
                  :body         (cheshire/generate-string {:title (str "Issue #")
                                                           :body  body})})
    issue-id))



