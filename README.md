# Contents
Spring Boot Application with:
 - Gradle Build
 - MongoDB via Repository API
 - Autoreload & IntelliJ Debugging
 - Kotlin ;)
 - Traditional RestController

# Todo
 - Speed up autoreload (maybe a restart is not required, but a autoreload only)
 - Remove MongoDB credentials from git

# Dev Setup

Debugging in IntelliJ \
https://www.jetbrains.com/help/idea/spring-boot.html

IntelliJ trigger Spring Autoreload \
https://stackoverflow.com/questions/12744303/intellij-idea-java-classes-not-auto-compiling-on-save/12744431#12744431

MongoDB Tutorial \
https://docs.spring.io/spring-data/mongodb/docs/2.1.2.RELEASE/reference/html/#mongodb-connectors

MongoDB \
* `cd data && docker-compose up`
* `./data/import.sh` to import some demo data

# Run
./gradlew bootRun