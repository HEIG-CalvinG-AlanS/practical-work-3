# Start from the Java 17 Temurin image
FROM eclipse-temurin:17

# Set the working directory
WORKDIR /app

# Copy the jar file
COPY target/practical-content-3-1.0-SNAPSHOT.jar /app/practical-content-3-1.0-SNAPSHOT.jar

# Set the entrypoint
ENTRYPOINT ["java", "-jar", "practical-content-3-1.0-SNAPSHOT.jar"]

# Set the default command
CMD ["--help"]