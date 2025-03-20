package events

import (
	"encoding/json"

	sensorsv1 "github.com/buehler/mcs-event-driven-systems/sensors/gen/events/sensors/v1"
	"github.com/buehler/mcs-event-driven-systems/sensors/internal/publisher"
	mqtt "github.com/eclipse/paho.mqtt.golang"
	"github.com/sirupsen/logrus"
)

type btnState = string

const (
	btnStatePressed  btnState = "PRESSED!"
	btnStateReleased btnState = "RELEASED!"
)

type Button struct {
	Event
	State string `json:"state"`
}

var lastBtnState = btnStateReleased

func OnClearanceBtnMessageReceived(_ mqtt.Client, msg mqtt.Message) {
	logger := logrus.WithField("topic", msg.Topic())
	logger.Info("Handle received message")

	var event Button
	if err := json.Unmarshal(msg.Payload(), &event); err != nil {
		logrus.Errorf("Failed to unmarshal button event: %v", err)
		return
	}

	if event.State == btnStatePressed && lastBtnState == btnStateReleased {
		publisher.SendKafkaEvent(&sensorsv1.AreaClearButtonPressed{})
		lastBtnState = btnStatePressed
		logger.Info("Button was pressed")
	} else if event.State == btnStateReleased && lastBtnState == btnStatePressed {
		lastBtnState = btnStateReleased
		logger.Info("Button was released")
	} else {
		logger.Warn("Button state not changed")
	}
}
