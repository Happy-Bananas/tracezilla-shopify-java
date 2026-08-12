FROM maven:3.9.9-eclipse-temurin-21
WORKDIR /app
COPY pom.xml ./
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn verify
ENTRYPOINT ["java", "-jar", "target/tracezilla-shopify-java-0.1.0.jar"]
