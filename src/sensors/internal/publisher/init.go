package publisher

import (
	"fmt"

	"github.com/buehler/mcs-event-driven-systems/sensors/internal/config"
	"github.com/confluentinc/confluent-kafka-go/kafka"
	"github.com/sirupsen/logrus"
)

var producer *kafka.Producer

func InitKafka() func() {
	logrus.Info("Initialize and setup Kafka")
	appConfig := config.GetConfig()

	cfg := kafka.ConfigMap{
		"bootstrap.servers": fmt.Sprintf("%s:%d", appConfig.KafkaHost, appConfig.KafkaPort),
	}

	p, err := kafka.NewProducer(&cfg)
	if err != nil {
		logrus.WithError(err).Fatal("Failed to create Kafka producer")
	}

	producer = p

	return producer.Close
}
