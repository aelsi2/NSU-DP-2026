(ns task-c5.core)

(defn make-forks [n]
  (map (fn [_] (ref 0)) (range n)))

(defn philosopher [i fork-l fork-r t-think t-dine n-dine]
  (if (pos? n-dine)
    (do (Thread/sleep t-think)
        (let [n-tries (atom 0)]
          (dosync
            (swap! n-tries inc)
            (alter fork-l inc)
            (alter fork-r inc)
            (Thread/sleep t-dine))
          (printf "%d ate in %d tries, %d left\n" i @n-tries (dec n-dine)) (flush))
        (recur i fork-l fork-r t-think t-dine (dec n-dine)))
    ))

(defn run-philosophers [n t-think t-dine n-dine]
  (let [forks (make-forks n)
        philosophers (doall (map (fn [i]
                                   (let [fork-l (nth forks i)
                                         fork-r (nth forks (rem (inc i) n))]
                                     (doto (Thread. #(philosopher i fork-l fork-r t-think t-dine n-dine))
                                       (.start))))
                                 (range n)))]
    (dorun (map ^[] Thread/.join philosophers))))