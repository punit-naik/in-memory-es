(ns org.clojars.punit-naik.in-memory-es-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [clojurewerkz.elastisch.rest :as esr]
            [clojurewerkz.elastisch.rest.document :as esd]
            [clojurewerkz.elastisch.rest.index :as esi]
            [org.clojars.punit-naik.in-memory-es :as imes]))

(defn create-mapping
  []
  {"person"
   {:properties
    {:username   {:type "string"
                  :store "yes"}
     :first-name {:type "string"
                  :store "yes"}
     :last-name  {:type "string"}
     :age        {:type "integer"}
     :title      {:type "string"
                  :analyzer "snowball"}
     :planet     {:type "string"}
     :biography  {:type "string"
                  :analyzer "snowball"
                  :term_vector "with_positions_offsets"}}}})

(def create-doc
  (memoize
   (fn
     [& [id]]
     (let [id (or id 0)]
       {:username (str "happyjoe-" id)
        :first-name (str "Joe-" id)
        :last-name (str "Smith-" id)
        :age (+ 30 id)
        :title (str "The Boss-" id)
        :planet (str "Earth-" id)
        :biography (str "N/A-" id)}))))

(def es-conn (atom nil))

(defn index-data
  [conn]
  (esi/create conn "test-index" {:mappings (create-mapping)})
  (Thread/sleep 1200)
  (esd/create conn "test-index" "person" (create-doc 1) {:id (:username (create-doc 1))})
  (esd/create conn "test-index" "person" (create-doc 2) {:id (:username (create-doc 2))})
  (esd/create conn "test-index" "person" (create-doc 3) {:id (:username (create-doc 3))}))

(defn setup-es
  [f]
  (let [node (imes/start)
        http-port (-> node .settings (.get "http.port"))
        conn (esr/connect (str "http://127.0.0.1:" http-port))]
    (reset! es-conn conn)
    (index-data conn)
    (f)
    (reset! es-conn nil)
    (imes/stop node)))

(use-fixtures :once setup-es)

(defn doc-with-id
  [conn id]
  (let [{:keys [_id] doc :_source}
        (esd/get conn "test-index" "person" id)]
    (assoc doc :_id _id)))

(deftest query-es-data-test
  (testing "Querying data created in the ES cluster during setup in fixture"
    (is (= (doc-with-id @es-conn (:username (create-doc 1)))
           (-> (create-doc 1)
               (assoc :_id (:username (create-doc 1))))))
    (is (= (doc-with-id @es-conn (:username (create-doc 2)))
           (-> (create-doc 2)
               (assoc :_id (:username (create-doc 2))))))
    (is (= (doc-with-id @es-conn (:username (create-doc 3)))
           (-> (create-doc 3)
               (assoc :_id (:username (create-doc 3))))))))