FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/batch-service-1.0.0.jar app.jar

EXPOSE 8084

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
