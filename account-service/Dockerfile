FROM amazoncorretto:21-alpine
WORKDIR /app
COPY target/*.jar account-service.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "account-service.jar"]
