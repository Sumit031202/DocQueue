FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/doc-queue-0.0.1-SNAPSHOT.jar .
EXPOSE 8080
ENTRYPOINT ["java","-jar","doc-queue-0.0.1-SNAPSHOT.jar"]