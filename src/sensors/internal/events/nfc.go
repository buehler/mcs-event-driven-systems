package events

import (
	"encoding/json"

	sensorsv1 "github.com/buehler/mcs-event-driven-systems/sensors/gen/events/sensors/v1"
	"github.com/buehler/mcs-event-driven-systems/sensors/internal/publisher"
	mqtt "github.com/eclipse/paho.mqtt.golang"
	"github.com/sirupsen/logrus"
)

type NFC struct {
	Event
	ID *string `json:"ID"`
}

var lastReading *string

func OnNFCMessageReceived(_ mqtt.Client, msg mqtt.Message) {
	logger := logrus.WithField("topic", msg.Topic())
	logger.Info("Handle received message")

	var event NFC
	if err := json.Unmarshal(msg.Payload(), &event); err != nil {
		logrus.Errorf("Failed to unmarshal nfc event: %v", err)
		return
	}

	if lastReading == nil && event.ID != nil {
		publisher.SendKafkaEvent(&sensorsv1.NFCObjectDetected{})
		lastReading = event.ID
		logger.Info("NFC object detected")
	} else if lastReading != nil && event.ID == nil {
		publisher.SendKafkaEvent(&sensorsv1.NFCObjectRemoved{})
		lastReading = nil
		logger.Info("NFC object removed")
	} else {
		logger.Warn("NFC object state not changed")
	}
}
