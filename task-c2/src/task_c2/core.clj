(ns task-c2.core)

(defn- next-prime
  "Given a vector of primes up to a certain number,
  finds the next prime after the last one and adds it to the vector."
  [primes]
  (let [previous (peek primes)
        start (inc' (or previous 1))]
    (->> (iterate inc' start)
         (filter (fn [candidate]
                   (let [potential-divisors (take-while #(<= (*' % %) candidate) primes)]
                     (not-any? #(zero? (mod candidate %)) potential-divisors))))
         (first)
         (conj primes))))

(def primes
  "An infinite sequence of prime numbers."
  (->> (iterate next-prime [2])
       (map peek)))