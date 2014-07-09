# stch.ns

Namespace utility for reloading modified files and any files that may depend on them. Designed to be used in a REPL. Uses [ns-tracker](https://github.com/weavejester/ns-tracker) under the hood.  Check out https://github.com/clojure/tools.namespace for a similar project.

## Installation

Add the following as a dependency to your lein user profile in ~/.lein/profiles.clj:

```clojure
[stch-library/ns "0.5.3"]
```

## How to use

stch.ns is designed with the assumption that namespaces are either 'use'd entirely or 'require'd (with or without an alias).  If a fn is found in the current namespace mapping, whose namespace matches the namespace of a modified file, the current mappings will be unmapped, and the namespace will be reloaded using 'use.'  Otherwise, the namespace will be reloaded using 'require.'  In either scenario functions that are removed from a namespace will be removed from the current namespace as well.

If you modify a file that other files depend on, all files will be reloaded in the correct order.

### Reloading

```clojure
(use 'stch.ns)

; Require or use your code.
(use 'me.core)
(require '[my.core :as my])

; Call after you've made a change to one or more namespaces.
(reload-ns)
; {use [me.core], require [my.core]}
```

### Setting source directories

The default tracking fn looks for modified files in the project's src directory.  To change this behavior create your own tracking fn like this.

```clojure
; Create a tracking fn that will look in the project's src and test directories.
(def tfn (mk-tracking-fn "src" "test"))

; Pass the custom tracking fn to reload-ns
(reload-ns tfn)
```

### Exceptions

What happens if an exception is thrown? Let's take a look at an example.

File: my/core.clj

```clojure
(ns my.core)

(/ 3 0)
```

REPL

```clojure
(reload-ns)
; {exceptions {my.core "Divide by zero"}}
```

### Unloading a namespace

What about unloading a namespace?  You can use unload-ns for that.

Namespace aliases

```clojure
(require '[my.core :as core])

(unload-ns 'my.core)

(require '[me.core :as core])
; No conflict exception, since we unloaded my.core.
```

Local mappings

```clojure
; Contains public var do-something
(use 'my.core)

(unload-ns 'my.core)

; Also contains a public var do-something
(use 'me.core)
; No conflict exception, since we unloaded my.core.
```

### use with glob patterns

use# accepts one or more glob patterns and loads the matching namespaces via use. Returns a vector of matched namespaces.

```clojure
(use# me.*)
; [me.core me.util]
```

If you have namespaces me.core and me.util, both will be loaded via use.

### Helper fns

stch.ns includes a few helper fns to help better understand what is currently loaded in the REPL and what namespaces exist in the src directory.

mappings returns a vector of fn name symbols that are mapped in the current namespace, and are defined in the given namespace.

```clojure
(use 'my.core)

(mappings 'my.core)
; [do-something]
```

search-ns# searches for namespaces along the classpath using glob patterns.

```clojure
(search-ns# me.*)
; (me.core me.util)
```

The clojure.core fn ns-aliases might also be helpful.

```clojure
(require '[me.core :as core])

(ns-aliases *ns*)
; {core #<Namespace me.core>}
```

## Things to note

1. stch.ns is designed to be used in the user namespace, and not in a project namespace.
2. If an exception is thrown in a namespace, that namespace will no longer be loaded in the current namespace.  An exceptions key will be added to result map, where each key/value pair is a namespace and the corresponding exception message.
3. You will need to create a record instance after a change to a protocol the record implements or the actual protocol implementation itself.
4. If no files have been modified since the last call to reload-ns, the return value will be nil. No namespaces will be reloaded in this case.
