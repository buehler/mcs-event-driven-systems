package publisher

import (
	"github.com/buehler/mcs-event-driven-systems/sensors/internal/config"
	"github.com/confluentinc/confluent-kafka-go/kafka"
	"github.com/sirupsen/logrus"
)

func SendKafkaEvent(msg []byte) {
	if producer == nil {
		logrus.Fatal("Kafka producer not initialized")
	}

	appConfig := config.GetConfig()
	kafkaMsg := kafka.Message{
		TopicPartition: kafka.TopicPartition{
			Topic:     &appConfig.KafkaTopic,
			Partition: kafka.PartitionAny,
		},
		Value: msg,
	}

	err := producer.Produce(&kafkaMsg, nil)
	if err != nil {
		logrus.WithError(err).Error("Failed to produce / send message")
		return
	}
	logrus.Info("Successfully forwarded Kafka message")
}
