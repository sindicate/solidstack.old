call ant buildtest
java -cp ..\BUILDS\solidstack\testclasses;..\BUILDS\solidstack\classes;lib\slf4j-api-1.6.1.jar;test\lib\logback-classic-1.0.0.jar;test\lib\logback-core-1.0.0.jar solidstack.cache.CacheTests
