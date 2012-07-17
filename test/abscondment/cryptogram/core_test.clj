(ns abscondment.cryptogram.core-test
  (:use [abscondment.cryptogram.core] :reload-all)
  (:use [clojure.test]))

(deftest famous-quotes
  (let [pairs
        [ ["gmy byjklqjpuy tjq jhjigk mctkyud gl gmy olbuh: gmy xqbyjklqjpuy lqy iybkckgk cq gbwcqe gl jhjig gmy olbuh gl mctkyud. gmybydlby juu iblebykk hyiyqhk lq gmy xqbyjklqjpuy tjq. (eylbey pybqjbh kmjo)"
           "the reasonable man adapts himself to the world: the unreasonable one persists in trying to adapt the world to himself. therefore all progress depends on the unreasonable man. (george bernard shaw)"]

          ["jevgvat n obbx vf n ybat, rkunhfgvat fgehttyr, yvxr n ybat obhg bs fbzr cnvashy vyyarff. bar jbhyq arire haqregnxr fhpu n guvat vs bar jrer abg qevira ol fbzr qrzba jubz bar pna arvgure erfvfg abe haqrefgnaq. (trbetr bejryy)"
           "Writing a book is a long, exhausting struggle, like a long bout of some painful illness. One would never undertake such a thing if one were not driven by some demon whom one can neither resist nor understand. (George Orwell)"]
          ]]
    (doseq [[encrypted decrypted] pairs]
      (let [solution (search {} (candidates-for (tokenize encrypted)))]
       (is (= (apply str (word-from-rules encrypted solution))
              (.toLowerCase decrypted)))))))

