# Cryptogram

```
;; Example Usage:
(time
 (println
  (let [code "Guvf vf n grfg." ;; "This is a test." in ROT13
        solution (search {} (candidates-for (tokenize code)))]
    (apply str (word-from-rules code solution))))))
```
