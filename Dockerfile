FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
# Giới hạn RAM cho Maven để tránh bị kill khi build trên các tier miễn phí
ENV MAVEN_OPTS="-Xmx256m"
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Cấu hình tối ưu RAM cho các free tier (như Render, Railway - thường có 512MB RAM)
# -Xmx256m: Giới hạn Heap size
# -XX:MaxMetaspaceSize=192m: Giới hạn Metadata để tránh "cạn metadata" (Metaspace OOM)
ENV JAVA_OPTS="-Xmx256m -XX:MaxMetaspaceSize=192m -Xss512k"

# Lệnh để khởi chạy ứng dụng
ENTRYPOINT ["sh", "-c", "java  -jar app.jar"]
