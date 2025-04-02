FROM bufbuild/buf:1.50 AS proto_builder
WORKDIR /src

COPY ./buf* ./
COPY ./src ./src

RUN buf generate


FROM golang:1.23 AS go_builder
WORKDIR /src

RUN apt-get update && apt-get install -y gcc librdkafka-dev

ENV KAFKA_NO_VENDOR=1 \
    CGO_ENABLED=1 \
    GOOS=linux

COPY src/test-kafka-publisher/go.mod src/test-kafka-publisher/go.sum ./
RUN --mount=type=cache,target=/go/pkg/mod go mod download

COPY --from=proto_builder /src/src/test-kafka-publisher .

RUN go build -tags dynamic -a -installsuffix cgo -o test-kafka-publisher ./


FROM debian:stable-slim
WORKDIR /app

RUN apt-get update && apt-get install -y librdkafka1 ca-certificates curl && rm -rf /var/lib/apt/lists/*

COPY --from=go_builder /etc/ssl/certs/ca-certificates.crt /etc/ssl/certs/
COPY --from=go_builder /src/test-kafka-publisher /app/test-kafka-publisher

USER 10001

ENTRYPOINT ["/app/test-kafka-publisher"]
