# Build stage
FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /build

COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -B -DskipTests

# Runtime stage
FROM eclipse-temurin:25-jre-jammy

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=builder /build/target/AudioGear-ECommerce.war /app/ROOT.war

RUN mkdir -p /usr/local/tomcat/webapps && \
    cp /app/ROOT.war /usr/local/tomcat/webapps/ && \
    rm /app/ROOT.war

ENV CATALINA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"
ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom -Dserver.port=10000"

EXPOSE 10000

CMD ["sh", "-c", "exec catalina.sh run"]