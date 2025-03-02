package config

import "github.com/kelseyhightower/envconfig"

type AppConfig struct {
	MqttBrokerAddress string `required:"true"`
}

var config AppConfig
var initialized bool

func InitConfig() error {
	err := envconfig.Process("sensors", config)
	initialized = true
	return err
}

func GetConfig() AppConfig {
	if !initialized {
		panic("Config not initialized")
	}

	return config
}
