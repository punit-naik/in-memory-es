(ns org.clojars.punit-naik.in-memory-es-test
  (:import [org.elasticsearch.common.settings Settings])
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest testing is use-fixtures]]
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

(deftest make-and-delete-dirs-test
  (let [dir-1 (io/file (imes/make-tmp-dir! "test" "dir-1"))
        dir-2 (io/file (imes/make-tmp-dir! "test" "dir-2"))]
    (is (.isDirectory dir-1))
    (is (.isDirectory dir-2))
    (imes/delete-dir! dir-1)
    (imes/delete-dir! dir-2)
    (is (false? (.isDirectory dir-1)))
    (is (false? (.isDirectory dir-2)))))

(deftest es-cluster-config-test
  (let [{:keys [http-port
                tmp-data-dir
                tmp-logs-dir
                cluster-name
                tmp-home-dir]
         :as arg-map}
        {:cluster-name "test"
         :http-port 9200
         :tmp-data-dir "/tmp-data-dir"
         :tmp-logs-dir "/tmp-logs-dir"
         :tmp-home-dir "/tmp-home-dir"}
        ^Settings settings (imes/es-cluster-config arg-map)]
    (is (= (.get settings "path.data") tmp-data-dir))
    (is (= (.get settings "path.logs") tmp-logs-dir))
    (is (= (.get settings "path.home") tmp-home-dir))
    (is (= (.get settings "cluster.name") cluster-name))
    (is (= (.get settings "http.port") (str http-port)))
    (is (= (.get settings "index.number_of_shards") "1"))
    (is (= (.get settings "index.number_of_replicas") "0"))
    (is (= (.get settings "node.local") "true"))))

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