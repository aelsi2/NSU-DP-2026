(ns task-c6.core)

;;;an empty route map
;;;it is enough to use either forward or backward part (they correspond to each other including shared reference to number of tickets)
;;;:forward is a map with route start point names as keys and nested map as values
;;;each nested map has route end point names as keys and route descriptor as values
;;;each route descriptor is a map (structure in fact) of the fixed structure where
;;;:price contains ticket price
;;;and :tickets contains reference to tickets number
;;;:backward has the same structure but start and end points are reverted
(def empty-map
  {:forward  {},
   :backward {}})

(defn route
  "Add a new route (route) to the given route map
   route-map - route map to modify
   from - name (string) of the start point of the route
   to - name (string) of the end poiunt of the route
   price - ticket price
   tickets-num - number of tickets available"
  [route-map from to price tickets-num]
  (let [tickets (ref tickets-num :validator (fn [state] (>= state 0))) ;reference for the number of tickets
        orig-source-desc (or (get-in route-map [:forward from]) {}),
        orig-reverse-dest-desc (or (get-in route-map [:backward to]) {}),
        route-desc {:price   price                          ;route descriptor
                    :tickets tickets},
        source-desc (assoc orig-source-desc to route-desc),
        reverse-dest-desc (assoc orig-reverse-dest-desc from route-desc)]
    (-> route-map
        (assoc-in [:forward from] source-desc)
        (assoc-in [:backward to] reverse-dest-desc))))

(defn make-nodes [route-map from]
  (let [forward-map (:forward route-map)]
    (->> (vals forward-map)
         (map keys)
         (flatten)
         (reduce (fn [node-map current-name]
                   (let [distance (if (= from current-name) 0 ##Inf)
                         neighbors (->> (get forward-map current-name)
                                        (filter (fn [[_ value]] (pos? @(:tickets value))))
                                        (map (fn [[key value]] [(:price value) key]))
                                        (vec))]
                     (assoc node-map current-name {:visited   false
                                                   :parent    nil
                                                   :distance  distance
                                                   :neighbors neighbors})))
                 (hash-map)))))

(defn make-queue [nodes]
  (->> nodes
       (map (fn [[key value]] [(:distance value) key]))
       (apply sorted-set)))

(defn make-result
  [nodes destination-name]
  (let [destination (get nodes destination-name)
        make-path (fn [path destination-name]
                    (let [destination (get nodes destination-name)
                          parent (:parent destination)
                          path (cons destination-name path)]
                      (cond (not parent) path
                            :else (recur path parent))))]
    (cond (infinite? (:distance destination)) {:error true}
          :else {:price (:distance destination)
                 :path  (vec (make-path '() destination-name))})))

(defn extract-queue [nodes queue]
  (let [first-element (first queue)
        first-name (second first-element)
        first-distance (first first-element)
        new-queue (disj queue first-element)]
    (cond (not first-element) [nil queue]
          (infinite? first-distance) [nil queue]
          (not (:visited (get nodes first-name))) [first-name new-queue]
          :else (recur nodes new-queue))))

(defn dijkstra [nodes queue destination]
  (let [extract-result (extract-queue nodes queue)
        current-name (first extract-result)
        queue (second extract-result)]
    (cond (not current-name) nodes
          (= current-name destination) nodes
          :else (let [current (get nodes current-name)
                      current-dist (:distance current)
                      current-neighbors (:neighbors current)
                      new-distances (->> current-neighbors
                                         (filter (fn [[_ name]] (not (:visited (get nodes name)))))
                                         (filter (fn [[dist name]]
                                                   (< (+ current-dist dist) (:distance (get nodes name)))))
                                         (map (fn [[dist name]] [(+ current-dist dist) name])))
                      current (assoc current :visited true)
                      nodes (assoc nodes current-name current)
                      nodes (reduce (fn [nodes [dist name]]
                                      (let [node (get nodes name)
                                            node (-> node
                                                     (assoc :distance dist)
                                                     (assoc :parent current-name))]
                                        (assoc nodes name node)))
                                    nodes new-distances)
                      queue (into queue new-distances)]
                  (recur nodes queue destination)))))

(def transaction-attempts (atom 0))

(defn book-tickets
  "Tries to book tickets and decrement appropriate references in route-map atomically
   returns map with either :price (for the whole route) and :path (a list of destination names) keys
          or with :error key that indicates that booking is impossible due to lack of tickets"
  [route-map from to]
  (if (= from to)
    {:path '(), :price 0}
    (dosync
      (swap! transaction-attempts inc)
      (let [nodes (make-nodes route-map from)
            queue (make-queue nodes)
            nodes (dijkstra nodes queue to)
            result (make-result nodes to)]
        (cond (:error result) result
              :else (doall (->>
                             (:path result)
                             (partition 2 1)
                             (map (fn [[from to]] (:tickets (get (get (:forward route-map) from) to))))
                             (map #(alter % dec)))))
        result))))

;;;cities
(def spec1 (-> empty-map
               (route "City1" "Capital" 200 5)
               (route "Capital" "City1" 250 5)
               (route "City2" "Capital" 200 5)
               (route "Capital" "City2" 250 5)
               (route "City3" "Capital" 300 3)
               (route "Capital" "City3" 400 3)
               (route "City1" "Town1_X" 50 2)
               (route "Town1_X" "City1" 150 2)
               (route "Town1_X" "TownX_2" 50 2)
               (route "TownX_2" "Town1_X" 150 2)
               (route "Town1_X" "TownX_2" 50 2)
               (route "TownX_2" "City2" 50 3)
               (route "City2" "TownX_2" 150 3)
               (route "City2" "Town2_3" 50 2)
               (route "Town2_3" "City2" 150 2)
               (route "Town2_3" "City3" 50 3)
               (route "City3" "Town2_3" 150 2)))

(defn booking-future [route-map from to init-delay loop-delay]
  (future
    (Thread/sleep init-delay)
    (loop [bookings []]
      (Thread/sleep loop-delay)
      (let [booking (book-tickets route-map from to)]
        (if (booking :error)
          bookings
          (recur (conj bookings booking)))))))

(defn print-bookings [name ft]
  (println (str name ":") (count ft) "bookings")
  (doseq [booking ft]
    (println "price:" (booking :price) "path:" (booking :path))))

(defn run []
  (reset! transaction-attempts 0)
  (let [f1 (booking-future spec1 "City1" "City3" 0 1),
        f2 (booking-future spec1 "City1" "City2" 0 1),
        f3 (booking-future spec1 "City2" "City3" 0 1)]
    (print-bookings "City1->City3:" @f1)
    (print-bookings "City1->City2:" @f2)
    (print-bookings "City2->City3:" @f3)
    (println "Total (re-)starts:" @transaction-attempts)))
