# Use OpenJDK 17 as a base image
FROM adoptopenjdk/openjdk17:alpine-jre

# Set the working directory in the container
WORKDIR /app

# Copy the packaged jar file into the container
COPY target/awspe.jar /app

# Command to run your application
CMD ["java", "-jar", "awspe.jar"]