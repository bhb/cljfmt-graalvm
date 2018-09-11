(ns cljfmt-graalvm.core
  (:require [cljfmt.core :as fmt]
            [clojure.java.io :as io]
            [clojure.tools.cli :as cli])
  (:gen-class))

;;;

(defn exists [path]
  (-> path
      (io/as-file)
      (.exists)))

(defn is-file [path]
  (-> path
      (io/as-file)
      (.isFile)))

(def cli-config
  [["-c" "--config CONFIG" "Path to config file"
    :validate [exists "Config path does not exist"
               is-file "Config path should refer to file"]]
   ["-h" "--help"]])

;;;

(defn reformat [opts file]
  (let [original (slurp file)
        revised (fmt/reformat-string original opts)]
    (when (not= (hash original) (hash revised))
      (println "Reformatting" file)
      (spit file revised))))

(defn process [opts files]
  (try
    (->> files
         (pmap (partial reformat opts))
         (doall))
    (catch Exception e
      (println (.getMessage e)))
    (finally
      (shutdown-agents))))

;;;

(defn -main [& args]
  (let [{:keys [arguments options summary]} (cli/parse-opts args cli-config)
        files arguments
        {:keys [config help]} options
        opts (or (some-> config slurp read-string) {})]
    (some->> config (println "Config: "))
    (if help
      (println summary)
      (process opts files))))
