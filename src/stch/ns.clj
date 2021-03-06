(ns stch.ns
  "Namespace utility for reloading modified files.
  Designed to be used in a REPL."
  (:use [ns-tracker.core :only [ns-tracker]]
        [bultitude.core :only [namespaces-on-classpath]]
        [stch.glob]))

(def ^:private modified-namespaces
  "Default namespace tracking fn.  Looks for modified
  files in the project src directory."
  (ns-tracker '("src")))

(defn- aliases
  "Takes a namespace symbol. Returns a vector of aliases
  for the given namespace or nil if the namespace could
  not be found."
  [ns-sym]
  (when-let [ns-obj (find-ns ns-sym)]
    (reduce (fn [acc [k v]]
              (if (= v ns-obj)
                (conj acc k)
                acc))
            []
            (ns-aliases *ns*))))

(defn- quote-form [f]
  `(quote ~f))

;;; Public fns

(defn src-ns
  "Returns a sequence of namespaces found in the
  src directory."
  []
  (namespaces-on-classpath :classpath "src"))

(defn search-ns
  "Given a glob pattern returns all the matching
  namespaces found on the classpath."
  [pattern]
  (let [namespaces (namespaces-on-classpath)
        pat (compile-pattern (name pattern))]
    (for [ns-sym namespaces
          :when (match-glob pat (str ns-sym))]
      ns-sym)))

(defmacro search-ns#
  "Slightly more convenient version of search-ns. Allows you to
  pass the glob pattern unquoted."
  [pattern]
  `(search-ns '~pattern))

(defn use*
  "Given one or more glob patterns, 'use' the matching
  namespaces.  Returns a vector of matched namespaces
  or nil. All namespaces found on the classpath
  will be checked."
  [& patterns]
  (let [namespaces (namespaces-on-classpath)
        matched (transient [])]
    (doseq [pattern patterns]
      (let [pat (compile-pattern (name pattern))]
        (doseq [ns-sym namespaces]
          (when (match-glob pat (str ns-sym))
            (use ns-sym)
            (conj! matched ns-sym)))))
    (let [matched (persistent! matched)]
      (when (seq matched)
        matched))))

(defmacro use#
  "Slightly more convenient version of use*. Allows you to
  pass glob patterns unquoted."
  [& patterns]
  `(use* ~@(map quote-form patterns)))

(defn mappings
  "Determine the fns that are mapped in the current
  namespace that are defined in the given namespace.
  Returns a vector of fn name symbols or nil if the
  namespace could not be found."
  [ns-sym]
  (when-let [ns-obj (find-ns ns-sym)]
    (->> (vals (ns-map *ns*))
         (map meta)
         (filter #(= (:ns %) ns-obj))
         (mapv :name))))

(defn mk-tracking-fn
  "Takes one or more directory strings and returns a
  tracking fn for those directories."
  [& dirs]
  (ns-tracker dirs))

(defn unload-ns
  "Takes a namespace symbol.  Unmaps any mapped
  vars and removes any namespace aliases.  The net
  effect is that you can use or require the same or
  a different namespace with having conflict exceptions
  thrown."
  [ns-sym]
  ; Unmap mapped vars
  (doseq [m (mappings ns-sym)]
    (ns-unmap *ns* m))
  ; Remove namespace aliases (if any exists)
  (doseq [a (aliases ns-sym)]
    (ns-unalias *ns* a)))

(defn reload-ns
  "Reload modified namespaces in dependency order.
  Optionally pass your own namespace tracking fn,
  generated using mk-tracking-fn. Returns a map
  of modified namespaces or nil."
  ([] (reload-ns modified-namespaces))
  ([ns-fn]
   ; Get modified namespaces and there mappings
   (let [modified
         (map (juxt identity mappings) (ns-fn))
         exceptions (transient {})]
     (doseq [[ns-sym ms] modified]
       ; Remove namespace
       (remove-ns ns-sym)
       ; Unmap mapped vars
       (doseq [m ms] (ns-unmap *ns* m))
       ; Attempt to reload namespace
       (try
         ((if (seq ms) use require) :reload ns-sym)
         (catch Exception e
           (assoc! exceptions ns-sym (.getMessage e)))))
     (let [exceptions (persistent! exceptions)]
       ; Return map of modified namespaces
       (reduce (fn [acc [ns-sym ms]]
                 (let [k (if (seq ms) 'use 'require)]
                   (cond (get-in acc ['exceptions ns-sym])
                         acc
                         (get acc k)
                         (update-in acc [k] conj ns-sym)
                         :else
                         (assoc acc k [ns-sym]))))
               (when (seq exceptions)
                 {'exceptions exceptions})
               modified)))))

(def rns "Shorthand for reload-ns"
  reload-ns)
