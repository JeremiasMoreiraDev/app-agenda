FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copia o JAR que já foi gerado na tua máquina para dentro do container
COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]