FROM FROM eclipse-temurin:21-jdk-alpine

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml first (for better caching)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Make mvnw executable
RUN chmod +x ./mvnw

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose port 8080
EXPOSE 8080

# Run the jar file (your artifact will be named Gestion-des-transports-0.0.1-SNAPSHOT.jar)
ENTRYPOINT ["java", "-jar", "target/Gestion-des-transports-0.0.1-SNAPSHOT.jar"]

# docker build -t gestion-transports .
# docker run -p 8080:8080 -e DB_URL_COVOIT=QQQQQQQQQQ -e DB_USER_COVOIT=QQQQQQQQ -e DB_PASS_COVOIT=QQQQQQQQQQQ gestion-transports
