FROM node:20-alpine AS frontend-builder
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

FROM eclipse-temurin:25-jdk AS backend-builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline
COPY src ./src
COPY --from=frontend-builder /app/frontend/dist ./src/main/resources/static/
RUN ./mvnw package -DskipTests

# Stage 3: Run
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=backend-builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", \
  "-Xms128m", \
  "-Xmx384m", \
  "-XX:+UseG1GC", \
  "-jar", "app.jar"]