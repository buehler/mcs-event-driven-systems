package publisher

import (
	"github.com/buehler/mcs-event-driven-systems/sensors/internal/config"
	"github.com/confluentinc/confluent-kafka-go/kafka"
	"github.com/sirupsen/logrus"
	"google.golang.org/protobuf/proto"
)

func SendKafkaEvent[TMsg proto.Message](protoMsg TMsg) {
	if producer == nil {
		logrus.Fatal("Kafka producer not initialized")
	}

	msgData, err := proto.Marshal(protoMsg)
	if err != nil {
		logrus.WithError(err).Error("Failed to marshal message")
		return
	}

	appConfig := config.GetConfig()
	msg := kafka.Message{
		TopicPartition: kafka.TopicPartition{
			Topic:     &appConfig.KafkaTopic,
			Partition: kafka.PartitionAny,
		},
		Value: msgData,
	}

	err = producer.Produce(&msg, nil)
	if err != nil {
		logrus.WithError(err).Error("Failed to produce / send message")
	}
	logrus.Info("Successfully sent Kafka message")
}
