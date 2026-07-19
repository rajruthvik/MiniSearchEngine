FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY MiniSearchEngine.java SearchApiServer.java ./
COPY data ./data

RUN javac MiniSearchEngine.java SearchApiServer.java

# Render/Railway inject PORT at runtime; SearchApiServer reads it via System.getenv("PORT")
CMD ["java", "SearchApiServer"]
