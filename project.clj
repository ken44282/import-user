(defproject import-user "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [com.layerware/hugsql-core "0.5.1"]
                 [com.layerware/hugsql-adapter-clojure-java-jdbc "0.5.1"]
                 [org.postgresql/postgresql "42.2.2"]
                 [org.clojure/data.csv "1.0.0"]
                 [org.clojure/tools.logging "1.1.0"]
                 [org.slf4j/slf4j-api "1.7.30"]
                 [org.apache.logging.log4j/log4j-slf4j-impl "2.14.0"]
                 [org.apache.logging.log4j/log4j-core "2.14.0"]
                 [cprop "0.1.17"]]
  :repl-options {:init-ns import-user.core}
  :jvm-opts ["-Dclojure.tools.logging.factory=clojure.tools.logging.impl/slf4j-factory"]
  :main import-user.core
  :aot [import-user.core]
  :profiles {:dev {:jvm-opts ["-Dconf=dev-config.edn" "-Dfile.encoding=MS932"]}})
