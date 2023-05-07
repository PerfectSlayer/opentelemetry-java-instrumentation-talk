FROM maven:3.9.1-eclipse-temurin-17 as builder
WORKDIR /app
COPY . ./
RUN mvn package

FROM eclipse-temurin:17
#RUN curl -Lo dd-java-agent.jar https://dtdg.co/latest-java-tracer
WORKDIR /var/storage
COPY --from=builder /app/target/storage-1.0.0-jar-with-dependencies.jar ./app.jar

#ENTRYPOINT ["java","-javaagent:../dd-java-agent.jar",  "-jar" , "build/libs/calendar-0.0.1-SNAPSHOT.jar"]
ENTRYPOINT ["java", "-jar" , "app.jar"]
EXPOSE 8080