package config

import "github.com/kelseyhightower/envconfig"

type AppConfig struct {
	MqttHost     string  `envconfig:"MQTT_HOST" required:"true"`
	MqttPort     uint16  `envconfig:"MQTT_PORT" default:"1883"`
	MqttUsername *string `envconfig:"MQTT_USER"`
	MqttPassword *string `envconfig:"MQTT_PASS"`

	KafkaHost  string `envconfig:"KAFKA_HOST" required:"true"`
	KafkaPort  uint16 `envconfig:"KAFKA_PORT" default:"9092"`
	KafkaTopic string `envconfig:"KAFKA_TOPIC" default:"events"`

	SensorsRotaryTopic        string  `envconfig:"SENSORS_ROTARY_TOPIC" default:"Tinkerforge/HumanWS/rotary_KVx"`
	SensorsNFCTopic           string  `envconfig:"SENSORS_NFC_TOPIC" default:"Tinkerforge/Conveyor/nfc_22Mp"`
	SensorsNFCDistTopic       string  `envconfig:"SENSORS_NFC_DIST_TOPIC" default:"Tinkerforge/HumanWS/distance_IR_short_TG2"`
	SensorsNFCDistThreshold   float32 `envconfig:"SENSORS_NFC_DIST_THRESHOLD" default:"7.0"`
	SensorsLeftDistTopic      string  `envconfig:"SENSORS_LEFT_DIST_TOPIC" default:"Tinkerforge/Conveyor/distance_IR_short_TFu"`
	SensorsLeftDistThreshold  float32 `envconfig:"SENSORS_LEFT_DIST_THRESHOLD" default:"9.2"`
	SensorsRightDistTopic     string  `envconfig:"SENSORS_RIGHT_DIST_TOPIC" default:"Tinkerforge/Conveyor/distance_IR_short_2a7C"`
	SensorsRightDistThreshold float32 `envconfig:"SENSORS_RIGHT_DIST_THRESHOLD" default:"9.2"`
	SensorsClearanceBtnTopic  string  `envconfig:"SENSORS_CLEARANCE_BTN_TOPIC" default:"Tinkerforge/HumanWS/rgb_button_24dY"`
}

var config AppConfig
var initialized bool

func InitConfig() error {
	err := envconfig.Process("sensors", &config)
	initialized = true
	return err
}

func GetConfig() AppConfig {
	if !initialized {
		panic("Config not initialized")
	}

	return config
}
