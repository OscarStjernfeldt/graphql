FROM eclipse-temurin:17-jre-alpine
EXPOSE 8080
COPY ./build/libs/graphql-0.0.1-SNAPSHOT.jar /graphql.jar
ENTRYPOINT ["java","-jar", "graphql.jar"]