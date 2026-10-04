(ns task-c2.core-test
  (:require [clojure.test :as test]
            [task-c2.core :as core]))

(test/deftest primes-first-20
  (test/testing "First 20 primes are correct"
    (test/is (= (list 2 3 5 7 11 13 17 19 23 29 31 37 41 43 47 53 59 61 67 71)
                (take 20 core/primes)))))

(test/deftest primes-distinct-5000
  (test/testing "First 5000 primes are actually distinct primes"
    (test/is (let [is-prime (fn [num] (not-any? zero? (map #(mod num %) (range 2 (/ num 2)))))]
               (= 5000 (->> (take 5000 core/primes)
                            (distinct)
                            (filter is-prime)
                            (count)))))))

(test/deftest primes-nth
  (test/testing "Nth prime is correct"
    (test/testing "29th"
      (test/is (= 109 (nth core/primes (dec 29)))))
    (test/testing "109th"
      (test/is (= 599 (nth core/primes (dec 109)))))
    (test/testing "599th"
      (test/is (= 4397 (nth core/primes (dec 599)))))
    (test/testing "1000th"
      (test/is (= 7919 (nth core/primes (dec 1000)))))
    (test/testing "69420th"
      (test/is (= 874651 (nth core/primes (dec 69420)))))))