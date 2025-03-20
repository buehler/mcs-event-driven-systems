package events

import (
	"encoding/json"

	sensorsv1 "github.com/buehler/mcs-event-driven-systems/sensors/gen/events/sensors/v1"
	"github.com/buehler/mcs-event-driven-systems/sensors/internal/publisher"
	mqtt "github.com/eclipse/paho.mqtt.golang"
	"github.com/sirupsen/logrus"
)

type Rotary struct {
	Event
	Position int32 `json:"position"`
}

var lastPosition int32

func OnRotaryMessageReceived(_ mqtt.Client, msg mqtt.Message) {
	logger := logrus.WithField("topic", msg.Topic())
	logger.Info("Handle received message")

	var event Rotary
	if err := json.Unmarshal(msg.Payload(), &event); err != nil {
		logrus.Errorf("Failed to unmarshal rotary event: %v", err)
		return
	}

	if lastPosition != event.Position {
		publisher.SendKafkaEvent(&sensorsv1.ConveyorSpeedChanged{Speed: event.Position})
		lastPosition = event.Position
		logger.Info("Conveyor speed changed")
	} else {
		logger.Warn("Conveyor speed not changed")
	}
}
