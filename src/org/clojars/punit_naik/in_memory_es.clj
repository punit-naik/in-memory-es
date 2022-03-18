(ns org.clojars.punit-naik.in-memory-es
  (:require [clojure.java.io :as io])
  (:import [org.elasticsearch.common.settings Settings]
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
  ^Settings
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
            ^Settings settings-builder (.builder settings)]
        (.put settings-builder "http.port" (str (inc http-port)))
        (recur
         (create-es-node
          (.build settings-builder)))))))

(defn start
  [& [cluster-name http-port]]
  (let [cluster-name (or cluster-name "test-cluster")
        http-port (or http-port 9200)
        tmp-data-dir (make-tmp-dir! cluster-name "data")
        tmp-logs-dir (make-tmp-dir! cluster-name "logs")
        tmp-home-dir (make-tmp-dir! cluster-name "home")
        ^Settings settings (es-cluster-config
                            {:cluster-name cluster-name
                             :http-port http-port
                             :tmp-data-dir tmp-data-dir
                             :tmp-logs-dir tmp-logs-dir
                             :tmp-home-dir tmp-home-dir})
        cluster (start-es-cluster settings)]
    cluster))

(defn delete-dir!
  [f]
  (when (.isDirectory f)
    (doseq [f2 (.listFiles f)]
      (delete-dir! f2)))
  (when (.exists f)
    (io/delete-file f)))

(defn stop
  [^Node cluster]
  (let [^Settings settings (.settings cluster)
        tmp-data-dir (io/file (.get settings "path.data"))
        tmp-logs-dir (io/file (.get settings "path.logs"))
        tmp-home-dir (io/file (.get settings "path.home"))]
    (.close cluster)
    (delete-dir! tmp-data-dir)
    (delete-dir! tmp-logs-dir)
    (delete-dir! tmp-home-dir)))
