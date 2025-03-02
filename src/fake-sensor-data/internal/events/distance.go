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

type distanceData struct {
	Type      string  `json:"type"`
	UID       string  `json:"UID"`
	Location  string  `json:"location"`
	MessageID string  `json:"messageID"`
	Distance  float32 `json:"distance"`
}

func sendDistanceEvents(ctx context.Context, wg *sync.WaitGroup) {
	logger := logrus.WithField("event", "distance")
	logger.Info("Setup and start sending distance events")

	appConfig := config.GetConfig()
	opts := mqtt.NewClientOptions()
	opts.AddBroker(fmt.Sprintf("tcp://%s:%d", appConfig.MqttHost, appConfig.MqttPort))
	opts.SetClientID("distance_events")

	client := mqtt.NewClient(opts)
	logger.Info("Connecting to mqtt broker")
	token := client.Connect()
	if token.Wait() && token.Error() != nil {
		logger.WithError(token.Error()).Fatal("Failed to connect to MQTT broker")
		return
	}

	go messagePublisher(messagePublisherConfig{
		typ:      "distance_IR_mid",
		uid:      "2a8w",
		location: "HumanWS",
		ctx:      ctx,
		wg:       wg,
		client:   &client,
		logger:   logger,
		payload: func(typ string, uid string, location string) interface{} {
			return distanceData{
				Type:      typ,
				UID:       uid,
				Location:  location,
				MessageID: fmt.Sprintf("%d", time.Now().UnixMilli()),
				Distance:  rand.Float32() * 100,
			}
		},
	})
	go messagePublisher(messagePublisherConfig{
		typ:      "distance_IR_short",
		uid:      "TG2",
		location: "HumanWS",
		ctx:      ctx,
		wg:       wg,
		client:   &client,
		logger:   logger,
		payload: func(typ string, uid string, location string) interface{} {
			return distanceData{
				Type:      typ,
				UID:       uid,
				Location:  location,
				MessageID: fmt.Sprintf("%d", time.Now().UnixMilli()),
				Distance:  rand.Float32() * 100,
			}
		},
	})
	go messagePublisher(messagePublisherConfig{
		typ:      "distance_IR_short",
		uid:      "TFu",
		location: "Conveyor",
		ctx:      ctx,
		wg:       wg,
		client:   &client,
		logger:   logger,
		payload: func(typ string, uid string, location string) interface{} {
			return distanceData{
				Type:      typ,
				UID:       uid,
				Location:  location,
				MessageID: fmt.Sprintf("%d", time.Now().UnixMilli()),
				Distance:  rand.Float32() * 100,
			}
		},
	})
	go messagePublisher(messagePublisherConfig{
		typ:      "distance_IR_short",
		uid:      "2a7c",
		location: "Conveyor",
		ctx:      ctx,
		wg:       wg,
		client:   &client,
		logger:   logger,
		payload: func(typ string, uid string, location string) interface{} {
			return distanceData{
				Type:      typ,
				UID:       uid,
				Location:  location,
				MessageID: fmt.Sprintf("%d", time.Now().UnixMilli()),
				Distance:  rand.Float32() * 100,
			}
		},
	})
	go messagePublisher(messagePublisherConfig{
		typ:      "distance_IR_long",
		uid:      "YZN",
		location: "Factory",
		ctx:      ctx,
		wg:       wg,
		client:   &client,
		logger:   logger,
		payload: func(typ string, uid string, location string) interface{} {
			return distanceData{
				Type:      typ,
				UID:       uid,
				Location:  location,
				MessageID: fmt.Sprintf("%d", time.Now().UnixMilli()),
				Distance:  rand.Float32() * 100,
			}
		},
	})

	<-ctx.Done()
	logger.Info("Shutting down distance events")
	client.Disconnect(250)
	logger.Info("Rotary events stopped")
}
