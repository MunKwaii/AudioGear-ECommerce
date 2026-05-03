# Build stage
FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /build

COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -B -DskipTests

# Runtime stage - use Tomcat image
FROM tomcat:10.1-jdk25

RUN rm -rf /usr/local/tomcat/webapps/ROOT

COPY --from=builder /build/target/AudioGear-ECommerce.war /usr/local/tomcat/webapps/ROOT.war

ENV CATALINA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"
ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom"
ENV PORT=10000

EXPOSE 10000

CMD ["catalina.sh", "run"]