package main

import (
	"context"
	"os/signal"
	"syscall"

	"github.com/buehler/mcs-event-driven-systems/sensors/internal/config"
	"github.com/buehler/mcs-event-driven-systems/sensors/internal/listener"
	"github.com/buehler/mcs-event-driven-systems/sensors/internal/publisher"
	"github.com/sirupsen/logrus"
)

func main() {
	logrus.Info("Starting up")
	err := config.InitConfig()
	if err != nil {
		logrus.WithError(err).Fatal("Failed to initialize config")
	}

	ctx, cancel := signal.NotifyContext(context.Background(), syscall.SIGINT, syscall.SIGTERM)
	defer cancel()

	kafka := publisher.InitKafka()
	defer kafka()
	go listener.StartMQTTListener(ctx)

	<-ctx.Done()
	logrus.Info("Shutting down")
}
