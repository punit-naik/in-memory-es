# in-memory-es

A Clojure library designed to work with an in-memory version of Elasticsearch for testing purposes.

**NOTE**: Has been written using `[org.elasticsearch/elasticsearch "2.4.6"]`, which is pretty old, but good enough for normal testing. Newest features won't work!

[![CircleCI](https://circleci.com/gh/punit-naik/in-memory-es/tree/master.svg?style=svg)](https://circleci.com/gh/punit-naik/in-memory-es/tree/master)
[![Clojars Project](https://img.shields.io/clojars/v/org.clojars.punit-naik/in-memory-es.svg)](https://clojars.org/org.clojars.punit-naik/in-memory-es)

## Usage

### Test

```
lein with-profiles +test test
```

### Code Coverage

[Code Coverage Report](https://punit-naik.github.io/in-memory-es/coverage)

## License

Copyright © 2022 [Punit Naik](https://github.com/punit-naik)

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
