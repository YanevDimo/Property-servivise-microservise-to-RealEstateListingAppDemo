FROM bellsoft/liberica-openjdk-alpine:17

WORKDIR /app

COPY target/property-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]

