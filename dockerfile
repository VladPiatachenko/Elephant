# First stage: build the project with Maven
FROM maven:3.6.3-openjdk-17-slim AS build
WORKDIR /app
# Copy only the POM file into the container
COPY pom.xml .

# Copy the rest of the Maven project files into the container
COPY src ./src
RUN mvn clean test package

# Second stage
# Use Ubuntu as the base image
FROM ubuntu:20.04

# Set the working directory
WORKDIR /app

# Install OpenJDK 17, curl, wget, unzip, and Maven
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk-headless curl wget unzip maven && \
    rm -rf /var/lib/apt/lists/*

# Copy the Java project files into the container
COPY . .

# Install Checkstyle
RUN curl -o checkstyle-8.44-all.jar https://github.com/checkstyle/checkstyle/releases/download/checkstyle-8.44/checkstyle-8.44-all.jar

# Download and extract PMD
RUN curl -o pmd-bin.zip -L https://github.com/pmd/pmd/releases/download/pmd_releases%2F6.42.0/pmd-bin-6.42.0.zip && \
    unzip pmd-bin.zip -d /app/pmd && \
    rm pmd-bin.zip

# Install SpotBugs
RUN curl -o spotbugs-4.3.0.tgz https://repo.maven.apache.org/maven2/com/github/spotbugs/spotbugs/4.3.0/spotbugs-4.3.0.tgz && \
    tar -xf spotbugs-4.3.0.tgz && \
    rm spotbugs-4.3.0.tgz

# Run Checkstyle, PMD, and SpotBugs
CMD java -jar checkstyle-8.44-all.jar -c checkstyle.xml src && \
    /app/pmd/bin/run.sh pmd -d src -R ruleset.xml && \
    spotbugs-4.3.0/bin/spotbugs -textui src


# Third stage: create a lightweight image with the compiled Java code
# Create missing folder and copy required files
#FROM --platform=$TARGETPLATFORM adoptopenjdk:16.0.1_9-jre-hotspot
#WORKDIR /app
#RUN mkdir "config"
#RUN mkdir "migrations"
#RUN apt-get update && \
#      apt-get -y install sudo
#COPY --from=build /app/target/elephant.jar elephant.jar
#COPY --from=build /app/target/classes/migrations/ migrations/
#CMD ["java", "-jar", "elephant.jar", "config/elephant.conf"]