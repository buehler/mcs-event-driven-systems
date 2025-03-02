FROM golang:1.23-alpine AS builder
WORKDIR /src

COPY go.mod go.sum ./
RUN --mount=type=cache,target=/go/pkg/mod go mod download

COPY . .

RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o fake-sensor-data ./cmd

FROM scratch

COPY --from=builder /etc/ssl/certs/ca-certificates.crt /etc/ssl/certs/
COPY --from=builder /src/fake-sensor-data /app/fake-sensor-data

USER 10001

ENTRYPOINT ["/app/fake-sensor-data"]
