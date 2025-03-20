package publisher

import (
	"reflect"

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

	msgType := reflect.ValueOf(protoMsg).Type().Elem().Name()

	appConfig := config.GetConfig()
	msg := kafka.Message{
		TopicPartition: kafka.TopicPartition{
			Topic:     &appConfig.KafkaTopic,
			Partition: kafka.PartitionAny,
		},
		Value: msgData,
		Headers: []kafka.Header{
			{Key: "messageType", Value: []byte(msgType)},
		},
	}

	err = producer.Produce(&msg, nil)
	if err != nil {
		logrus.WithError(err).Error("Failed to produce / send message")
		return
	}
	logrus.WithField("messageType", msgType).Info("Successfully sent Kafka message")
}
