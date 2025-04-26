FROM golang:1.23 AS go_builder
WORKDIR /src

RUN apt-get update && apt-get install -y gcc librdkafka-dev

ENV KAFKA_NO_VENDOR=1 \
    CGO_ENABLED=1 \
    GOOS=linux

COPY src/sensors/go.mod src/sensors/go.sum ./
RUN --mount=type=cache,target=/go/pkg/mod go mod download

COPY src/sensors .

RUN go build -tags dynamic -a -installsuffix cgo -o sensors ./cmd


FROM debian:stable-slim
WORKDIR /app

RUN apt-get update && apt-get install -y librdkafka1 ca-certificates curl && rm -rf /var/lib/apt/lists/*

COPY --from=go_builder /etc/ssl/certs/ca-certificates.crt /etc/ssl/certs/
COPY --from=go_builder /src/sensors /app/sensors

USER 10001

ENTRYPOINT ["/app/sensors"]
