package events

import (
	"encoding/json"

	sensorsv1 "github.com/buehler/mcs-event-driven-systems/sensors/gen/events/sensors/v1"
	mqtt "github.com/eclipse/paho.mqtt.golang"
	"github.com/sirupsen/logrus"
	"google.golang.org/protobuf/proto"
	"google.golang.org/protobuf/types/known/timestamppb"
)

type NFC struct {
	Event
	ReadingID byte    `json:"readingID"`
	ID        *string `json:"ID"`
}

func processNFC(id, location string, msg mqtt.Message) proto.Message {
	var event NFC
	if err := json.Unmarshal(msg.Payload(), &event); err != nil {
		logrus.Errorf("Failed to unmarshal nfc event: %v", err)
		return nil
	}

	if event.ReadingID == 0 || event.ID == nil {
		return nil
	}

	return &sensorsv1.SensorEvent{
		SensorId:  id,
		Location:  location,
		Timestamp: timestamppb.Now(),
		Data: &sensorsv1.SensorEvent_Nfc{
			Nfc: &sensorsv1.NFCData{
				Id: *event.ID,
			},
		},
	}
}
