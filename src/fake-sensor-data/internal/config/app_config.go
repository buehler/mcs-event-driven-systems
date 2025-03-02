package config

import "github.com/kelseyhightower/envconfig"

type AppConfig struct {
	MqttHost string `envconfig:"MQTT_HOST" required:"true"`
	MqttPort uint16 `envconfig:"MQTT_PORT" default:"1883"`
}

var config AppConfig
var initialized bool

func InitConfig() error {
	err := envconfig.Process("", &config)
	initialized = true
	return err
}

func GetConfig() AppConfig {
	if !initialized {
		panic("Config not initialized")
	}

	return config
}
