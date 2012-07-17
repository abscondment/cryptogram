# Cryptogram

## What

Using a local word list, generates and searches the cryptogram solution space.

### Future Work

 * Use word frequency to determine the order in which candidate words are tested. This will let us return the most likely solution when there are multiple solutions, rather than randomly picking the first one.

## Usage

```
abscondment.cryptogram.core> ;; Example Usage:
(time
 (println
  (let [code (.toLowerCase "Gur Puevfgvna vqrny unf abg orra gevrq naq sbhaq jnagvat; vg unf orra sbhaq qvssvphyg naq yrsg hagevrq.\n-Purfgregba")
       solution (search {} (candidates-for (tokenize code)))]
       (->> solution (word-from-rules code) (apply str)))))
the christian ideal has not been tried and found wanting; it has been found difficult and left untried.
-chesterton
"Elapsed time: 1417.705818 msecs"    
```

## License

Copyright (c) Brendan Ribera. All rights reserved.

The use and distribution terms for this software are covered by the Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which can be found in the file epl-v10.html at the root of this distribution. By using this software in any fashion, you are agreeing to be bound by the terms of this license.

You must not remove this notice, or any other, from this software.
