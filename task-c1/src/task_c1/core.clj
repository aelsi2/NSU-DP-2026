(ns task-c1.core)

(defn cartesian
  "Computes the cartesian product of two lists. Function func is used for creating pairs."
  [a b func]
  (reduce
    (fn [acc elem] (concat acc elem))
    (map (fn [a-i]
           (map (fn [b-i] (func a-i b-i)) b)) a)))

(defn cartesian-power
  "Computes the cartesian power of a list. Function func is used for creating pairs."
  [a n func]
  (reduce (fn [acc elem] (cartesian acc elem func))
          (repeat n a)))

(defn has-equal-neighbors
  "Checks if the list has any equal elements that are direct neighbors."
  [lst]
  (reduce #(or %1 %2)
          (map = lst (rest lst))))

(defn all-words-no-dup-neighbors
  "Creates a list of words of alphabet alpha (a list of 1-char long strings) and length n
  where no immediately neighboring characters are equal."
  [alpha n]
  (remove has-equal-neighbors
          (cartesian-power alpha n String/.concat)))