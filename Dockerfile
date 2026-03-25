FROM alpine:latest
RUN apk update && apk add --no-cache curl
RUN apk add --no-cache openjdk25
COPY target/app.jar /app.jar
EXPOSE 7070
CMD ["java", "-jar", "/app.jar"]
