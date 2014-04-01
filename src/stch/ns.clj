(ns stch.ns
  "Namespace utility for reloading modified files.
  Designed to be used in a REPL."
  (:use ns-tracker.core))

(def mk-tracking-fn
  "Takes a list of directory strings and returns a
  tracking fn."
  ns-tracker)

(def modified-namespaces
  "Default namespace tracking fn.  Looks for modified
  files in the project src directory."
  (ns-tracker ["src"]))

(defn mappings
  "Determine which fns are mapped in the current
  namespace that are defined in the given namespace.
  Returns a vector of fn name symbols or nil."
  [ns-sym]
  (when (find-ns ns-sym)
    (->> (vals (ns-map *ns*))
         (map meta)
         (filter #(= (:ns %) (the-ns ns-sym)))
         (mapv :name))))

(defn reload-ns
  "Reload modified namespaces in dependency order.
  Optionally pass your own namespace tracking fn,
  generated using mk-tracking-fn. Returns
  a list of reloaded namespaces or nil."
  ([] (reload-ns modified-namespaces))
  ([ns-fn]
   ; Get modified namespaces
   (let [modified (ns-fn)]
     (doseq [ns-sym modified]
       ; Get namespace mappings
       (let [ms (mappings ns-sym)]
         ; Remove namespace
         (remove-ns ns-sym)
         ; Unmap mapped vars
         (doseq [m ms]
           (ns-unmap *ns* m))
         ; Reload namespace
         ((if (seq ms) use require) :reload ns-sym)))
     modified)))








