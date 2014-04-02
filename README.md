# stch.ns

Namespace utility for reloading modified files and any files that may depend on them. Designed to be used in a REPL. Uses [ns-tracker](https://github.com/weavejester/ns-tracker) under the hood.  Check out https://github.com/clojure/tools.namespace for a similar project.

## Installation

Add the following as a dependency to your lein user profile in ~/.lein/profiles.clj:

```clojure
[stch-library/ns "0.3.0"]
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
; {use [me.core], require [my.core]}
```

The default tracking fn looks for modified files in the project's src directory.  To change this behavior create your own tracking fn like this.

```clojure
; Create a tracking fn that will look in the project's src and test directories.
(def tfn (mk-tracking-fn ["src" "test"]))

; Pass the custom tracking fn to reload-ns
(reload-ns tfn)
```

## Things to note

1. stch.ns is designed to be used in the user namespace, and not in a project namespace.
2. If an exception is thrown in a namespace, that namespace will no longer be loaded in the current namespace.  An exceptions key will be added to result map, where each key/value pair is a namespace and the corresponding exception message.
3. You will need to create a record instance after a change to a protocol the record implements or the actual protocol implementation itself.
4. If no files have been modified since the last call to reload-ns, the return value will be nil. No namespaces will be reloaded in this case.











