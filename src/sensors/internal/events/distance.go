package events

import (
	"encoding/json"

	sensorsv1 "github.com/buehler/mcs-event-driven-systems/sensors/gen/events/sensors/v1"
	mqtt "github.com/eclipse/paho.mqtt.golang"
	"github.com/sirupsen/logrus"
	"google.golang.org/protobuf/proto"
	"google.golang.org/protobuf/types/known/timestamppb"
)

type Distance struct {
	Event
	Distance float32 `json:"distance"`
}

func processDistance(id, location string, msg mqtt.Message) proto.Message {
	var event Distance
	if err := json.Unmarshal(msg.Payload(), &event); err != nil {
		logrus.Errorf("Failed to unmarshal distance event: %v", err)
		return nil
	}

	return &sensorsv1.SensorEvent{
		SensorId:  id,
		Location:  location,
		Timestamp: timestamppb.Now(),
		Data: &sensorsv1.SensorEvent_Distance{
			Distance: &sensorsv1.DistanceData{
				Distance: event.Distance,
			},
		},
	}
}
