(defproject in-memory-es "0.1.0"
  :description "A Clojure library designed to work with an in-memory version of Elasticsearch for testing purposes."
  :url "https://github.com/punit-naik/in-memory-es"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [net.java.dev.jna/jna "5.10.0"]
                 [org.elasticsearch/elasticsearch "2.4.6"]]
  :profiles {:test {:dependencies [[clojurewerkz/elastisch "3.0.1"]]}}
  :repl-options {:init-ns org.clojars.punit-naik.in-memory-es})
