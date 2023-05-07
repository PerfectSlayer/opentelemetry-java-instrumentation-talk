FROM openjdk:17-buster
WORKDIR /app

RUN apt-get update -y; apt-get install curl -y
RUN curl -Lo dd-java-agent.jar https://dtdg.co/latest-java-tracer

COPY . ./
RUN ./mvnw package
ENTRYPOINT ["java", "-javaagent:dd-java-agent.jar",  "-jar", "target/order-1.0.0.jar"]
EXPOSE 8081
