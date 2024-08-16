# Use JDK 17 as the base image
FROM openjdk:17-jdk-alpine
RUN apk update

# Define the ARG for the JAR file
ARG JAR_FILE=target/*.jar

# Copy the JAR file to the container
COPY ${JAR_FILE} app.jar


# Expose the port your application runs on
EXPOSE 8080
EXPOSE 3306

# Define the entry point for the container
ENTRYPOINT ["java", "-jar", "/app.jar"]
