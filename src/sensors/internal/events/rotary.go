package events

import (
	"encoding/json"

	sensorsv1 "github.com/buehler/mcs-event-driven-systems/sensors/gen/events/sensors/v1"
	mqtt "github.com/eclipse/paho.mqtt.golang"
	"github.com/sirupsen/logrus"
	"google.golang.org/protobuf/proto"
	"google.golang.org/protobuf/types/known/timestamppb"
)

type Rotary struct {
	Event
	Position uint16 `json:"position"`
}

func processRotary(id, location string, msg mqtt.Message) proto.Message {
	var event Rotary
	if err := json.Unmarshal(msg.Payload(), &event); err != nil {
		logrus.Errorf("Failed to unmarshal rotary event: %v", err)
		return nil
	}

	return &sensorsv1.SensorEvent{
		SensorId:  id,
		Location:  location,
		Timestamp: timestamppb.Now(),
		Data: &sensorsv1.SensorEvent_Rotary{
			Rotary: &sensorsv1.RotaryData{
				Position: uint32(event.Position),
			},
		},
	}
}
