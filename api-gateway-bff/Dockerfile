FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
COPY target/api-gateway-bff-1.0.0.jar app.jar

# Crear directorio para keystore
RUN mkdir -p /app/keystore

# Copiar el keystore (se generará con el script de generación de certificados)
COPY src/main/resources/keystore/bank-bff.p12 /app/keystore/bank-bff.p12

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
