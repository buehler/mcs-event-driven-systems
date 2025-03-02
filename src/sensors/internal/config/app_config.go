package config

import "github.com/kelseyhightower/envconfig"

type AppConfig struct {
	MqttHost     string  `envconfig:"MQTT_HOST" required:"true"`
	MqttPort     uint16  `envconfig:"MQTT_PORT" default:"1883"`
	MqttUsername *string `envconfig:"MQTT_USER"`
	MqttPassword *string `envconfig:"MQTT_PASS"`

	KafkaHost  string `envconfig:"KAFKA_HOST" required:"true"`
	KafkaPort  uint16 `envconfig:"KAFKA_PORT" default:"9092"`
	KafkaTopic string `envconfig:"KAFKA_TOPIC" required:"true"`
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
