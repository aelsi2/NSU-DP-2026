(ns task-c3.core)

(defn p-filter
  "Filters the collection in parallel, splitting it into chunks and launching a separate job for each chunk.
  The size of each chunk is specified by the chunk-size parameter. Default chunk size is 16.
  The maximum number of parallel jobs is specified by num-jobs.
  The default value of num-jobs matches the number of logical CPUs in the system."
  ([f coll] (let [chunk-size 16
                  num-jobs (.availableProcessors (Runtime/getRuntime))]
              (p-filter chunk-size num-jobs f coll)))
  ([chunk-size num-jobs f coll]
   (letfn [(cat-all [colls]
             (lazy-seq (if (seq colls)
                         (concat (first colls) (cat-all (rest colls))))))]
     (if (<= chunk-size 0) (throw (RuntimeException. "Chunk size must be positive.")))
     (if (<= num-jobs 0) (throw (RuntimeException. "Job count must be positive.")))
     (->> coll
          (partition-all chunk-size)
          (map #(future (doall (filter f %))))
          (partition-all num-jobs 1)
          (map #(deref (first (doall %))))
          (cat-all)))))