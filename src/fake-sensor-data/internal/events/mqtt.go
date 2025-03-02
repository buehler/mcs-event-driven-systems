package events

import (
	"context"
	"encoding/json"
	"fmt"
	"math/rand"
	"sync"
	"time"

	mqtt "github.com/eclipse/paho.mqtt.golang"
	"github.com/sirupsen/logrus"
)

type payloadCreator = func(typ string, uid string, location string) interface{}

type messagePublisherConfig struct {
	typ      string
	uid      string
	location string
	logger   *logrus.Entry
	ctx      context.Context
	wg       *sync.WaitGroup
	client   *mqtt.Client
	payload  payloadCreator
}

func messagePublisher(cfg messagePublisherConfig) {
	defer cfg.wg.Done()
	cfg.wg.Add(1)

	logger := cfg.logger.WithField("type", cfg.typ).
		WithField("uid", cfg.uid).
		WithField("location", cfg.location)
	topic := fmt.Sprintf("Tinkerforge/%s/%s_%s", cfg.location, cfg.typ, cfg.uid)
	for {
		select {
		case <-cfg.ctx.Done():
			return
		default:
			data := cfg.payload(cfg.typ, cfg.uid, cfg.location)

			// Marshal the data to JSON.
			payload, err := json.Marshal(data)
			if err != nil {
				logger.WithError(err).Error("Failed to marshal data")
				continue
			}

			// Publish the message to topic.
			pubToken := (*cfg.client).Publish(topic, 0, false, payload)
			pubToken.Wait()
			if pubToken.Error() != nil {
				logger.WithError(pubToken.Error()).Error("Failed to publish data")
			} else {
				logger.Info("Successfully published data")
			}

			// Wait for a random duration between 500ms and 2500ms.
			sleepDuration := time.Duration(rand.Intn(2000)+500) * time.Millisecond
			time.Sleep(sleepDuration)
		}
	}
}
