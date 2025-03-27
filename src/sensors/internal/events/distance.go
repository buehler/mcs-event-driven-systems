package events

import (
	"encoding/json"

	v1 "github.com/buehler/mcs-event-driven-systems/sensors/gen/events/sensors/v1"
	"github.com/buehler/mcs-event-driven-systems/sensors/internal/config"
	"github.com/buehler/mcs-event-driven-systems/sensors/internal/publisher"
	mqtt "github.com/eclipse/paho.mqtt.golang"
	"github.com/sirupsen/logrus"
)

type distanceSensor = byte

const (
	leftDist  distanceSensor = 0
	rightDist distanceSensor = 1
)

type Distance struct {
	Event
	Distance float32 `json:"distance"`
}

func OnLeftDistMessageReceived(_ mqtt.Client, msg mqtt.Message) {
	logger := logrus.WithField("topic", msg.Topic())
	logger.Info("Handle received message")
	processDistMessageReceived(leftDist, msg)
}

func OnRightDistMessageReceived(_ mqtt.Client, msg mqtt.Message) {
	logger := logrus.WithField("topic", msg.Topic())
	logger.Info("Handle received message")
	processDistMessageReceived(rightDist, msg)
}

var distanceSensorsStates = map[distanceSensor]bool{
	leftDist:  false,
	rightDist: false,
}

func processDistMessageReceived(dist distanceSensor, msg mqtt.Message) {
	var event Distance
	if err := json.Unmarshal(msg.Payload(), &event); err != nil {
		logrus.Errorf("Failed to unmarshal distance event: %v", err)
		return
	}

	appConfig := config.GetConfig()
	distThreshold := float32(0)
	if dist == leftDist {
		distThreshold = appConfig.SensorsLeftDistThreshold
	} else {
		distThreshold = appConfig.SensorsRightDistThreshold
	}

	if !distanceSensorsStates[dist] && event.Distance <= distThreshold {
		// send detected
		if dist == leftDist {
			publisher.SendKafkaEvent(&v1.LeftObjectDetected{})
			logrus.Info("Left distance sensor object detected")
		} else {
			publisher.SendKafkaEvent(&v1.RightObjectDetected{})
			logrus.Info("Right distance sensor object detected")
		}
		distanceSensorsStates[dist] = true
	} else if distanceSensorsStates[dist] && event.Distance > distThreshold {
		// send removed
		if dist == leftDist {
			publisher.SendKafkaEvent(&v1.LeftObjectRemoved{})
			logrus.Info("Left distance sensor object removed")
		} else {
			publisher.SendKafkaEvent(&v1.RightObjectRemoved{})
			logrus.Info("Right distance sensor object removed")
		}
		distanceSensorsStates[dist] = false
	} else {
		logrus.Warn("Distance sensor state does not make sense.")
	}
}
