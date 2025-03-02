package events

import (
	"context"
	"fmt"
	"math/rand/v2"
	"sync"
	"time"

	"github.com/buehler/mcs-event-driven-systems/fake-sensor-data/internal/config"
	mqtt "github.com/eclipse/paho.mqtt.golang"
	"github.com/sirupsen/logrus"
)

type nfcData struct {
	Type      string  `json:"type"`
	UID       string  `json:"UID"`
	Location  string  `json:"location"`
	MessageID string  `json:"messageID"`
	ReadingID byte    `json:"readingID"`
	ID        *string `json:"ID"`
}

func sendNFCEvents(ctx context.Context, wg *sync.WaitGroup) {
	logger := logrus.WithField("event", "nfc")
	logger.Info("Setup and start sending nfc events")

	appConfig := config.GetConfig()
	opts := mqtt.NewClientOptions()
	opts.AddBroker(fmt.Sprintf("tcp://%s:%d", appConfig.MqttBrokerAddress, appConfig.MqttPort))
	opts.SetClientID("nfc_events")

	client := mqtt.NewClient(opts)
	logger.Info("Connecting to mqtt broker")
	token := client.Connect()
	if token.Wait() && token.Error() != nil {
		logger.WithError(token.Error()).Fatal("Failed to connect to MQTT broker")
		return
	}

	go messagePublisher(messagePublisherConfig{
		typ:      "nfc",
		uid:      "22Mp",
		location: "Conveyor",
		ctx:      ctx,
		wg:       wg,
		client:   &client,
		logger:   logger,
		payload: func(typ string, uid string, location string) interface{} {
			d := nfcData{
				Type:      typ,
				UID:       uid,
				Location:  location,
				MessageID: fmt.Sprintf("%d", time.Now().UnixMilli()),
				ReadingID: rand.N[byte](2),
			}

			if d.ReadingID == 1 {
				id := "ID 0x04 0x2F 0xAC 0x4A 0x61 0x60 0x80"
				d.ID = &id
			}

			return d
		},
	})

	<-ctx.Done()
	logger.Info("Shutting down nfc events")
	client.Disconnect(250)
	logger.Info("NFC events stopped")
}
