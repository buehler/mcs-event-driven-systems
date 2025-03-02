package main

import (
	"context"
	"os/signal"
	"sync"
	"syscall"

	"github.com/buehler/mcs-event-driven-systems/fake-sensor-data/internal/config"
	"github.com/buehler/mcs-event-driven-systems/fake-sensor-data/internal/events"
	"github.com/sirupsen/logrus"
)

func main() {
	logrus.Info("Starting up fake MQTT data publisher")
	err := config.InitConfig()
	if err != nil {
		logrus.WithError(err).Fatal("Failed to initialize config")
	}

	ctx, cancel := signal.NotifyContext(context.Background(), syscall.SIGINT, syscall.SIGTERM)
	defer cancel()

	var wg sync.WaitGroup
	events.StartSendingEvents(ctx, &wg)

	<-ctx.Done()
	logrus.Info("Shutting down fake MQTT data publisher")
	wg.Wait()
	logrus.Info("Gracefully shut down")
}
