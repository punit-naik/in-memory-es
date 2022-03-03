(ns org.clojars.punit-naik.in-memory-es
  (:require [clojure.java.io :as io])
  (:import [java.util HashMap Map]
           [org.elasticsearch.common.settings Settings]
           [org.elasticsearch.node Node]
           [org.elasticsearch.node NodeBuilder]))

(defn make-tmp-dir!
  [cluster-name dir-name]
  (let [base-dir (System/getProperty "java.io.tmpdir")
        base-name (str (System/currentTimeMillis) "-" (long (rand 1000000000)))
        custom-name (str cluster-name "-" dir-name)
        tmp-dir (io/file (str base-dir "/" base-name "-" custom-name))]
    (.mkdir tmp-dir)
    (str tmp-dir)))

(defn es-cluster-config
  [{:keys [http-port tmp-data-dir tmp-logs-dir cluster-name tmp-home-dir]}]
  (-> (Settings/builder)
      (.put "http.port" http-port)
      (.put "path.data" tmp-data-dir)
      (.put "path.logs" tmp-logs-dir)
      (.put "path.home" tmp-home-dir)
      (.put "cluster.name" cluster-name)
      (.put "index.number_of_shards" 1)
      (.put "index.number_of_replicas" 0)
      (.put "node.local" "true")
      .build))

(defn create-es-node
  [^Settings settings]
  (try (-> (NodeBuilder.)
           (.settings settings)
           .node)
       (catch Exception e (.getMessage e))))

(defn start-es-cluster
  [^Settings settings]
  (loop [node (create-es-node settings)]
    (if (instance? Node node)
      ^Node node
      (let [http-port (Integer/parseInt (.get settings "http.port"))
            ^Map settings-map (HashMap. (.getAsMap settings))]
        (.put settings-map "http.port" (str (inc http-port)))
        (recur
         (create-es-node
          (-> (Settings/builder)
              (.put settings-map)
              .build)))))))

(defn start
  [& [cluster-name http-port]]
  (let [cluster-name (or cluster-name "test-cluster")
        http-port (or http-port 9200)
        tmp-data-dir (make-tmp-dir! cluster-name "data")
        tmp-logs-dir (make-tmp-dir! cluster-name "logs")
        tmp-home-dir (make-tmp-dir! cluster-name "home")
        settings (es-cluster-config
                  {:cluster-name cluster-name
                   :http-port http-port
                   :tmp-data-dir tmp-data-dir
                   :tmp-logs-dir tmp-logs-dir
                   :tmp-home-dir tmp-home-dir})
        cluster (start-es-cluster settings)]
    cluster))

(defn stop
  [^Node cluster]
  (.close cluster))
