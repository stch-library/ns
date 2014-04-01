# stch.ns

Namespace utility for reloading modified files and any files that may depend on them. Designed to be used in a REPL. Uses [ns-tracker](https://github.com/weavejester/ns-tracker) under the hood.  Check out https://github.com/clojure/tools.namespace for a similar project.

## Installation

Add the following as a dependency to your lein user profile in ~/.lein/profiles.clj:

```clojure
[stch-library/ns "0.1.0"]
```

## How to use

stch.ns is designed with the assumption that namespaces are either 'use'd entirely or 'require'd (with or without an alias).  If a fn is found in the current namespace mapping, whose namespace matches the namespace of a modified file, the current mappings will be unmapped, and the namespace will be reloaded using 'use.'  Otherwise, the namespace will be reloaded using 'require.'  In either scenario functions that are removed from a namespace will be removed from the current namespace as well.

If you modify a file that other files depend on, all files will be reloaded in the correct order.

```clojure
(use 'stch.ns)

; Require or use your code.
(use 'me.core)
(require '[my.core :as my])

; Call after you've made a change to one or more namespaces.
(reload-ns)
```

The default tracking fn looks for modified files in the project's src directory.  To change this behavior create your own tracking fn like this.

```clojure
; Create a tracking fn that will look in the project's src and test directories.
(def tfn (mk-tracking-fn ["src" "test"]))

; Pass the custom tracking fn to reload-ns
(reload-ns tfn)
```













