(ns task-c3.core-test
  (:require [clojure.test :as test]
            [task-c3.core :as core]))

(defn heavy-even [num]
  (Thread/sleep 1)
  (even? num))

(test/deftest empty-sequence
  (test/testing "The function handles an empty sequence"
    (test/is (= (list)
                (core/p-filter heavy-even (list))))))

(test/deftest finite-sequence
  (test/testing "The function handles a finite sequence"
    (test/is (= (list 0 2 4 6 8)
                (core/p-filter heavy-even (range 0 10))))))

(test/deftest sequence-of-sequences
  (test/testing "The function handles a sequence of sequences"
    (test/is (= (list (list 0 0 0) (list 2 2 2) (list 4 4 4) (list 6 6 6) (list 8 8 8))
                (core/p-filter #(heavy-even (first %)) (map #(list % % %) (range 0 10)))))))

(test/deftest lazy-sequence
  (test/testing "The function handles a lazy sequence"
    (test/is (= (list 0 2 4 6 8 10 12 14 16 18)
                (take 10 (core/p-filter heavy-even (iterate inc 0)))))))

(test/deftest time-p-filter
  (test/testing "Times the parallel filter implementation"
    (println "p-filter:")
    (time (dorun(core/p-filter heavy-even (range 0 100))))))

(test/deftest time-filter
  (test/testing "Times the sequential filter implementation for comparison"
    (println "filter:")
    (time (dorun(filter heavy-even (range 0 100))))))

(test/deftest large-chunk-size
  (test/testing "The function handles a large chunk size"
    (test/is (= (list 0 2 4 6 8)
                (core/p-filter 500 4 heavy-even (range 0 10))))))

(test/deftest large-jobs
  (test/testing "The function handles a large chunk size"
    (test/is (= (list 0 2 4 6 8)
                (core/p-filter 16 500 heavy-even (range 0 10))))))

(test/deftest large-jobs-and-chunk-size
  (test/testing "The function handles a large chunk size"
    (test/is (= (list 0 2 4 6 8)
                (core/p-filter 500 500 heavy-even (range 0 10))))))

(test/deftest zero-chunk-size
  (test/testing "The function handles chunk size equal to 0"
    (test/is (thrown? RuntimeException
                (core/p-filter 0 4 heavy-even (range 0 10))))))

(test/deftest zero-jobs
  (test/testing "The function handles number of jobs equal to 0"
    (test/is (thrown? RuntimeException
                      (core/p-filter 10 0 heavy-even (range 0 10))))))

(test/deftest negative-chunk-size
  (test/testing "The function handles a negative chunk size"
    (test/is (thrown? RuntimeException
                      (core/p-filter -5 4 heavy-even (range 0 10))))))

(test/deftest negative-jobs
  (test/testing "The function handles a negative number of jobs"
    (test/is (thrown? RuntimeException
                      (core/p-filter 10 -5 heavy-even (range 0 10))))))