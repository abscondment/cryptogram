;; Copyright (C) Brendan Ribera 2009-2012
(ns abscondment.cryptogram.core
  (:require [clojure.string :as string]))

(defn tokenize
  "Simple tokenization of a block of text."
  [text]
  (string/split (string/replace (.toLowerCase text) #"[^a-z\s]+" "")
                #"[\s]+"))

(defn base-form
  "Compute the base form of a given word. E.g. 'seas' and 'that' become 'ABCA'."
  [w]
  (loop [idx 65
         seen (transient {})
         letters (seq w)]
    (if (empty? letters)
      (persistent! seen)
      (if (get seen (first letters))
        (recur idx seen (rest letters))
        (recur (inc idx)
               (assoc! seen (first letters) (char idx))
               (rest letters))))))

(defn word-from-rules
  "Apply character transformation rules to a given word."
  [word rules]
  (map #(or (get rules %) %) (seq word)))

(defn candidates-for
  "Return a list of pairs: an encrypted word and a set of candidate decrypted words."
  [coll]
  (let [coll (distinct coll)
        sizes (set (map count coll))
        bf-map
        (loop [accepted (transient {})
               remaining (tokenize (slurp "/usr/share/dict/american-english"))]
          (if (empty? remaining)
            (persistent! accepted)
            (let [[check & next-remaining] remaining
                  next-accepted (if (contains? sizes (count check))
                                  (let [mapped-check (base-form check)
                                        mapped-key (word-from-rules
                                                    check
                                                    mapped-check)
                                        mapped-list (or (get accepted mapped-key) #{})]
                                    (assoc!
                                     accepted
                                     mapped-key
                                     (conj mapped-list (list check mapped-check))))
                                  accepted)]
              (recur next-accepted next-remaining))))]
    (map #(list % (get bf-map (word-from-rules % (base-form %))))
         coll)))

(defn update-rules
  "Given an existing set of rules and a new decryption, return updated rules."
  [rules encrypted decrypted]
  (reduce #(apply assoc %1 %2) rules (partition 2 (interleave encrypted decrypted))))

(defn propagate
  "Apply a set of rules to a candidate list, returning the new candidate list or nil if an inconsistency is encountered. If any candidate is confirmed as thet only choice, also propagate that choice."
  [rules candidate-pairs]
  (let [candidate-pairs
        (map
         (fn [coll]
           ;; For a given word, eliminate candidates that no longer match our rules.
           (let [encrypted (first coll)
                 candidates (last coll)
                 new-bf-map (merge (base-form encrypted) rules)
                 new-key (word-from-rules encrypted new-bf-map)]
             (list
              encrypted
              (filter
               #(= new-key
                   (word-from-rules
                    (first %)
                    (merge (last %)
                           (apply hash-map
                                  (mapcat (fn [r] (list r r))
                                          (vals rules))))))
               candidates))))
         candidate-pairs)]
    ;; Now that we've eliminated candidates, check for inconsistency.
    (if (some #(= 0 (count (last %))) candidate-pairs)
      nil
      (let [solved-words (filter #(= 1 (count (last %))) candidate-pairs)]
        (if (or (not candidate-pairs) (empty? solved-words))
          ;; If we had no inconsistency and no newly solved words, return.
          {:rules rules :candidates candidate-pairs}
          ;; Otherwise, create new rules for the new solved words and propagate them.
          (let [new-rules (reduce
                           merge
                           rules
                           (map #(let [[encrypted [[decrypted _]]] %]
                                   (reduce (fn [a b] (apply assoc a b)) {}
                                           (partition 2 (interleave encrypted decrypted))))
                                solved-words))]
            ;; Make sure the new rules are actually different.
            (if (= rules new-rules)
              {:rules rules :candidates candidate-pairs}
              (recur new-rules candidate-pairs))))))))

(defn search
  "Given rules and candidates, perform a depth-first search of potential word decryptions until we find a valid solution."
  [rules candidates]
  (let [candidate-counts (map #(-> % rest first count) candidates)]
    (cond
     ;; Are we in an inconsistent state?
     (or (not candidates) (empty? candidates) (some #(= 0 %) candidate-counts)) nil
     ;; Are we in a solved state?
     (every? #(= 1 %) candidate-counts)
     rules
     ;; Keep searching.
     true
     (let [candidate (first (sort-by
                             #(-> % rest first count)
                             (filter
                              #(not (= 1 (-> % rest first count)))
                              candidates)))
           encrypted (first candidate)]
       (first
        (filter
         #(and %)
         (map #(let [new-rules (update-rules rules encrypted (first %))
                     result (propagate new-rules candidates)]
                 (if result (search (result :rules) (result :candidates))))
              (fnext candidate))))))))