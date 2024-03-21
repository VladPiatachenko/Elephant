# First stage: build the project with Maven
FROM --platform=$BUILDPLATFORM maven:3.6.3-openjdk-16-slim AS build
WORKDIR /app
COPY . .
RUN mvn clean -e -B package -Dmaven.test.skip=true

# Second stage: create a lightweight image with the compiled Java code
# Create missing folder and copy required files
FROM --platform=$TARGETPLATFORM adoptopenjdk:16.0.1_9-jre-hotspot
WORKDIR /app
RUN mkdir "config"
RUN mkdir "migrations"
RUN apt-get update && \
      apt-get -y install sudo
COPY --from=build /app/target/elephant.jar elephant.jar
COPY --from=build /app/target/classes/migrations/ migrations/
CMD ["java", "-jar", "elephant.jar", "config/elephant.conf"]
