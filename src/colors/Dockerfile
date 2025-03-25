FROM bufbuild/buf:1.50 AS proto_builder
WORKDIR /src

COPY ./buf* ./
COPY ./src ./src

RUN buf generate


FROM gradle:8.13-jdk21 AS builder
WORKDIR /src

COPY --from=proto_builder /src/src/colors .

RUN gradle clean build --no-daemon


FROM openjdk:21-slim
WORKDIR /app

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

COPY --from=builder /src/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
