# events

A Java library and a command line tool that parses various event stream sources (logs, Java runtimes), turns them into events streams and assists with analysis or makes it easy to feed into R.

# Documentation

https://kb.novaordis.com/index.php/esa

# Maven Tests

```
mvn test -Dmaven.surefire.debug="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5012" -Dtest=HttpdLogEventFactoryTest
```

