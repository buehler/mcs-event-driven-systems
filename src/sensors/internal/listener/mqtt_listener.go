package listener

import (
	"context"
	"fmt"

	"github.com/buehler/mcs-event-driven-systems/sensors/internal/config"
	"github.com/buehler/mcs-event-driven-systems/sensors/internal/events"
	mqtt "github.com/eclipse/paho.mqtt.golang"
	"github.com/sirupsen/logrus"
)

func StartMQTTListener(ctx context.Context) {
	appConfig := config.GetConfig()

	opts := mqtt.NewClientOptions()
	opts.AddBroker(fmt.Sprintf("tcp://%s:%d", appConfig.MqttHost, appConfig.MqttPort))
	opts.SetClientID("sensors_mqtt_listener")
	opts.SetOnConnectHandler(onConnect)
	opts.SetConnectionLostHandler(onConnectionLost)

	if appConfig.MqttUsername != nil {
		opts.SetUsername(*appConfig.MqttUsername)
	}
	if appConfig.MqttPassword != nil {
		opts.SetPassword(*appConfig.MqttPassword)
	}

	logrus.Info("Starting MQTT sensor data processor")
	client := mqtt.NewClient(opts)
	if token := client.Connect(); token.Wait() && token.Error() != nil {
		logrus.WithError(token.Error()).Fatal("Failed to connect to MQTT broker")
	}
	defer client.Disconnect(250)

	// Subscribe to NFC sensor data
	if token := client.Subscribe(appConfig.SensorsNFCTopic, 0, events.OnNFCMessageReceived); token.Wait() && token.Error() != nil {
		logrus.WithError(token.Error()).Fatal("Failed to subscribe to MQTT topic")
	}

	// Subscribe to rotary sensor data
	if token := client.Subscribe(appConfig.SensorsRotaryTopic, 0, events.OnRotaryMessageReceived); token.Wait() && token.Error() != nil {
		logrus.WithError(token.Error()).Fatal("Failed to subscribe to MQTT topic")
	}

	// Subscribe to left distance sensor data
	if token := client.Subscribe(appConfig.SensorsLeftDistTopic, 0, events.OnLeftDistMessageReceived); token.Wait() && token.Error() != nil {
		logrus.WithError(token.Error()).Fatal("Failed to subscribe to MQTT topic")
	}

	// Subscribe to right distance sensor data
	if token := client.Subscribe(appConfig.SensorsRightDistTopic, 0, events.OnRightDistMessageReceived); token.Wait() && token.Error() != nil {
		logrus.WithError(token.Error()).Fatal("Failed to subscribe to MQTT topic")
	}

	// Subscribe to NFC distance sensor data
	if token := client.Subscribe(appConfig.SensorsNFCDistTopic, 0, events.OnNFCDistMessageReceived); token.Wait() && token.Error() != nil {
		logrus.WithError(token.Error()).Fatal("Failed to subscribe to MQTT topic")
	}

	// Subscribe to clearance button sensor data
	if token := client.Subscribe(appConfig.SensorsClearanceBtnTopic, 0, events.OnClearanceBtnMessageReceived); token.Wait() && token.Error() != nil {
		logrus.WithError(token.Error()).Fatal("Failed to subscribe to MQTT topic")
	}

	<-ctx.Done()
	logrus.Info("Shutting down MQTT sensor data processor")
}

func onConnect(_ mqtt.Client) {
	logrus.Info("Connected to MQTT broker")
}

func onConnectionLost(_ mqtt.Client, err error) {
	logrus.WithError(err).Fatal("Connection to MQTT broker lost")
}
