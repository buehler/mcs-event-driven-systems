FROM golang:1.23-alpine AS go_builder
WORKDIR /src

COPY go.mod go.sum ./
RUN --mount=type=cache,target=/go/pkg/mod go mod download

COPY . .

RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o sensors ./cmd

FROM scratch

COPY --from=go_builder /etc/ssl/certs/ca-certificates.crt /etc/ssl/certs/
COPY --from=go_builder /src/sensors /app/sensors

USER 10001

ENTRYPOINT ["/app/sensors"]
