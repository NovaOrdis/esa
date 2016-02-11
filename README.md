# esa
Event Stream Analyzer

# Documentation

https://kb.novaordis.com/index.php/esa

# Maven Tests

```
mvn test -Dmaven.surefire.debug="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5012" -Dtest=HttpdLogEventFactoryTest
```

