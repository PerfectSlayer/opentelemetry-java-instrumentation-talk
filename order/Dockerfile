FROM eclipse-temurin:17
WORKDIR /app
COPY . ./
RUN ./mvnw package
ENTRYPOINT ["./mvnw", "spring-boot:run"]
EXPOSE 8081
