Redis backed java Map implementation.
This impl is bit frugal and do not follow strict map semantics. Many of the methods throw UnsupportedOperationException.
This could be used as starting point to further improve upon it. Impl internally rely on spring-data-redis. So spring dependency is MUST.
