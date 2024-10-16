# Liquibase Schema
Manage Camunda Database Schema [updates](https://docs.camunda.org/manual/latest/installation/database-schema) <mark>automatically</mark> with [Liquibase](https://www.liquibase.org/).

## Why
* By default, Camunda doesn't provide the automatic way to update the database schema from older version. There is a `camunda.bpm.database.schema-update` property which seems to be the way to activate automatic schema updates. However, it does not work (if set to `true`) or the meaning is different.
* You could only create the initial database (for example in version `7.21`) with Spring Boot Starter, but after Framework upgrade (for example to the new version `7.22`) you need to upgrade database schema [manually](https://docs.camunda.org/manual/latest/installation/database-schema/).

## How
* Deactivate Camunda database `schema-update` to be managed by Liquibase and reference the `changelog.xml`:
```yaml
camunda.bpm:
  database:
    # managed by liquibase
    schema-update: false
spring:
  liquibase:
    # camunda changesets
    change-log: db/changelog.xml
```
* Create a Spring config which tells Camunda engine to wait until Liquibase step is done
```java
@Slf4j
@Component
@RequiredArgsConstructor
@RequiredBy("repositoryService")
public class LiquibaseConfig {

  private final SpringLiquibase liquibase;

  @PostConstruct
  private void init() {
    log.info("Liquibase initialized: {}", liquibase.getChangeLog());
  }

}
```
* Create a [db/changelog.xml](src/main/resources/db/changelog.xml) like this.
You could also add some custom changesets like additional indexes which may be missing but important for performance.
```xml
<?xml version="1.0" encoding="UTF-8"?>

<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.3.xsd">

  <!-- Camunda baseline (starts with version 7.16.0 -->

  <property name="db.name" value="cockroachdb" dbms="cockroachdb"/>
  <property name="db.name" value="db2" dbms="db2"/>
  <property name="db.name" value="h2" dbms="h2"/>
  <property name="db.name" value="mariadb" dbms="mariadb"/>
  <property name="db.name" value="mssql" dbms="mssql"/>
  <property name="db.name" value="mysql" dbms="mysql"/>
  <property name="db.name" value="oracle" dbms="oracle"/>
  <property name="db.name" value="postgres" dbms="postgresql"/>

  <property name="camunda.db.root" value="org/camunda/bpm/engine/db"/>
  <property name="camunda.liquibase.root" value="${camunda.db.root}/liquibase"/>

  <changeSet author="Camunda" id="7.16.0-baseline">
    <sqlFile path="${camunda.liquibase.root}/baseline/liquibase.${db.name}.create.engine.sql"
             relativeToChangelogFile="false"
             splitStatements="true"
             stripComments="true"/>
    <sqlFile path="${camunda.liquibase.root}/baseline/liquibase.${db.name}.create.history.sql"
             relativeToChangelogFile="false"
             splitStatements="true"
             stripComments="true"/>
    <sqlFile path="${camunda.liquibase.root}/baseline/liquibase.${db.name}.create.identity.sql"
             relativeToChangelogFile="false"
             splitStatements="true"
             stripComments="true"/>
    <sqlFile path="${camunda.liquibase.root}/baseline/liquibase.${db.name}.create.case.engine.sql"
             relativeToChangelogFile="false"
             splitStatements="true"
             stripComments="true"/>
    <sqlFile path="${camunda.liquibase.root}/baseline/liquibase.${db.name}.create.case.history.sql"
             relativeToChangelogFile="false"
             splitStatements="true"
             stripComments="true"/>
    <sqlFile path="${camunda.liquibase.root}/baseline/liquibase.${db.name}.create.decision.engine.sql"
             relativeToChangelogFile="false"
             splitStatements="true"
             stripComments="true"/>
    <sqlFile path="${camunda.liquibase.root}/baseline/liquibase.${db.name}.create.decision.history.sql"
             relativeToChangelogFile="false"
             splitStatements="true"
             stripComments="true"/>
  </changeSet>

  <changeSet author="Camunda" id="7.16.0-tag">
    <tagDatabase tag="7.16.0"/>
  </changeSet>

  <changeSet author="Camunda" id="7.16-to-7.17">
    <sqlFile path="${camunda.db.root}/upgrade/${db.name}_engine_7.16_to_7.17.sql"
             relativeToChangelogFile="false"
             splitStatements="true"
             stripComments="true"/>
  </changeSet>

  <changeSet author="Camunda" id="7.17.0-tag">
    <tagDatabase tag="7.17.0"/>
  </changeSet>

  <changeSet author="Camunda" id="7.17-to-7.18">
    <sqlFile path="${camunda.db.root}/upgrade/${db.name}_engine_7.17_to_7.18.sql"
             relativeToChangelogFile="false"
             splitStatements="true"
             stripComments="true"/>
  </changeSet>

  <changeSet author="Camunda" id="7.18.0-tag">
    <tagDatabase tag="7.18.0"/>
  </changeSet>

  <changeSet author="Camunda" id="7.18-to-7.19">
    <sqlFile path="${camunda.db.root}/upgrade/${db.name}_engine_7.18_to_7.19.sql"
             relativeToChangelogFile="false"
             splitStatements="true"
             stripComments="true"/>
  </changeSet>

  <changeSet author="Camunda" id="7.19.0-tag">
    <tagDatabase tag="7.19.0"/>
  </changeSet>

  <changeSet author="Camunda" id="7.19-to-7.20">
    <sqlFile path="${camunda.db.root}/upgrade/${db.name}_engine_7.19_to_7.20.sql"
             relativeToChangelogFile="false"
             splitStatements="true"
             stripComments="true"/>
  </changeSet>

  <changeSet author="Camunda" id="7.20.0-tag">
    <tagDatabase tag="7.20.0"/>
  </changeSet>

  <changeSet author="Camunda" id="7.20-to-7.21">
    <sqlFile path="${camunda.db.root}/upgrade/${db.name}_engine_7.20_to_7.21.sql"
             relativeToChangelogFile="false"
             splitStatements="true"
             stripComments="true"/>
  </changeSet>

  <changeSet author="Camunda" id="7.21.0-tag">
    <tagDatabase tag="7.21.0"/>
  </changeSet>

  <changeSet author="Camunda" id="7.21-to-7.22">
    <sqlFile path="${camunda.db.root}/upgrade/${db.name}_engine_7.21_to_7.22.sql"
             relativeToChangelogFile="false"
             splitStatements="true"
             stripComments="true"/>
  </changeSet>

  <changeSet author="Camunda" id="7.22.0-tag">
    <tagDatabase tag="7.22.0"/>
  </changeSet>

  <!-- Our scripts -->

</databaseChangeLog>
```