package events

import (
	"context"
	"fmt"
	"math/rand"
	"sync"
	"time"

	"github.com/buehler/mcs-event-driven-systems/fake-sensor-data/internal/config"
	mqtt "github.com/eclipse/paho.mqtt.golang"
	"github.com/sirupsen/logrus"
)

type rotaryData struct {
	Type      string `json:"type"`
	UID       string `json:"UID"`
	Location  string `json:"location"`
	MessageID string `json:"messageID"`
	Position  uint16 `json:"position"`
}

func sendRotaryEvents(ctx context.Context, wg *sync.WaitGroup) {
	logger := logrus.WithField("event", "rotary")
	logger.Info("Setup and start sending rotary events")

	appConfig := config.GetConfig()
	opts := mqtt.NewClientOptions()
	opts.AddBroker(fmt.Sprintf("tcp://%s:%d", appConfig.MqttHost, appConfig.MqttPort))
	opts.SetClientID("rotary_events")

	client := mqtt.NewClient(opts)
	logger.Info("Connecting to mqtt broker")
	token := client.Connect()
	if token.Wait() && token.Error() != nil {
		logger.WithError(token.Error()).Fatal("Failed to connect to MQTT broker")
		return
	}

	go messagePublisher(messagePublisherConfig{
		typ:      "rotary",
		uid:      "ABC",
		location: "HumanWS",
		ctx:      ctx,
		wg:       wg,
		client:   &client,
		logger:   logger,
		payload: func(typ string, uid string, location string) interface{} {
			return rotaryData{
				Type:      typ,
				UID:       uid,
				Location:  location,
				MessageID: fmt.Sprintf("%d", time.Now().UnixMilli()),
				Position:  uint16(rand.Intn(360)),
			}
		},
	})

	<-ctx.Done()
	logger.Info("Shutting down rotary events")
	client.Disconnect(250)
	logger.Info("Rotary events stopped")
}
