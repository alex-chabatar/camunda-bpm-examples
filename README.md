# Camunda BPM Examples

Various examples for Camunda Platform 7.

* [Incidents Resolver](incidents-resolver/README.md) - Technical BPMN process to resolve Camunda Incidents
* [MyBatis Custom Mappings](mybatis-custom-mappings/README.md) - Camunda extension for Process/Task comments
* [Process Instance Migration](process-instance-migration/README.md) - Camunda extension to schedule Migration batch on application start

## Build and Run
* Required: Java 17

### Maven
```
mvn clean install
```

### OWASP dependency check:
```
mvn dependency-check:aggregate -Pdependency-check
```

## License

[Apache License, Version 2.0](./LICENSE)

