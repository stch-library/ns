# History

## 0.5.1

1. Split out glob functionality into separate project.

## 0.5.0

1. Added bultitude 0.2.6 as a dependency.
2. Added src-ns, search-ns, and use* fns.

## 0.4.0

1. Added unload-ns fn.
2. mk-tracking fn is variadic now instead of taking a vector.
3. Exposed helper fn: mappings.

## 0.3.0

1. Add exceptions to result map.

## 0.2.0

1. Output of reload-ns returns a map, where keys are use/require and values are a vector of namespaces that were reloaded with the corresponding loading fn.

## 0.1.0

Initial release.
