package events

import (
	"strings"

	"github.com/buehler/mcs-event-driven-systems/sensors/internal/publisher"
	mqtt "github.com/eclipse/paho.mqtt.golang"
	"github.com/sirupsen/logrus"
	"google.golang.org/protobuf/proto"
)

var (
	allowedLocations = map[string]bool{
		"HumanWS":  true,
		"Conveyor": true,
		"Factory":  true,
	}
	allowedTypes = map[string]bool{
		"nfc":               true,
		"rotary":            true,
		"distance_IR_mid":   true,
		"distance_IR_short": true,
		"distance_IR_long":  true,
	}
)

func HandleReceivedMQTTMessage(msg mqtt.Message) {
	topic := msg.Topic()
	topic, _ = strings.CutPrefix(topic, "Tinkerforge/")

	parts := strings.Split(topic, "/")
	if len(parts) != 2 {
		logrus.Warnf("Invalid topic format: %s", topic)
		return
	}

	location := parts[0]
	if !allowedLocations[location] {
		logrus.Warnf("Invalid location: %s", location)
		return
	}

	rest := parts[1]
	idx := strings.LastIndex(rest, "_")
	if idx == -1 {
		logrus.Warnf("Topic missing underscore separator for type and id: %s", topic)
		return
	}
	eventType := rest[:idx]
	sensorId := rest[idx+1:]

	if !allowedTypes[eventType] {
		logrus.Warnf("Invalid event type: %s", eventType)
		return
	}

	var protoMsg proto.Message
	switch eventType {
	case "rotary":
		protoMsg = processRotary(sensorId, location, msg)
	case "nfc":
		protoMsg = processNFC(sensorId, location, msg)
	case "distance_IR_mid", "distance_IR_short", "distance_IR_long":
		protoMsg = processDistance(sensorId, location, msg)
	default:
		logrus.Warnf("Unsupported event type: %s", eventType)
		return
	}

	if protoMsg == nil {
		return
	}

	logrus.Infof("Send Kafka MSG for %s", topic)
	publisher.SendKafkaEvent(protoMsg)
}
